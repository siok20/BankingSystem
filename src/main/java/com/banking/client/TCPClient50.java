package com.banking.client;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient50 {

    private String servermsj;
    public  String SERVER_IP;
    public static final int SERVER_PORT = 8080;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    public TCPClient50(String ip,OnMessageReceived listener) {
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
            System.out.println("Socket Client Connecting...");
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            System.out.println("Sucessfully");
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("Cliente conectado.");

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun) {
                    servermsj = in.readLine();
                    if (servermsj != null && mMessageListener != null) {
                        mMessageListener.messageReceived(servermsj);
                        //System.out.println(servermsj);
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