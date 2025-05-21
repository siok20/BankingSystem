/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.worknode;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author siok
 */
public class WorkNode {
    private String servermsj;
    public  String SERVER_IP;
    public static final int SERVER_PORT = 8080;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;
    
    public WorkNode(String ip,OnMessageReceived listener) {
        SERVER_IP = ip;
        mMessageListener = listener;
    }
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }
    public void stopClient(){
        mRun = false;
    }
    public void run() {
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            System.out.println("TCP Client"+ "C: Conectando...");
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("TCP Client"+ "C: Sent.");
                System.out.println("TCP Client"+ "C: Done.");
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun) {
                    servermsj = in.readLine();
                    if (servermsj != null && mMessageListener != null) {
                        mMessageListener.messageReceived(servermsj);
                    }
                    servermsj = null;
                }
            } catch (Exception e) {
                System.out.println("TCP"+ "S: Error"+e);

            } finally {
                socket.close();
            }
        } catch (Exception e) {
            System.out.println("TCP"+ "C: Error"+ e);
        }
    }
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
