/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.worknode;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

/**
 *
 * @author siok
 */
public class WorkNode {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]); // puerto como argumento
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Nodo escuchando en puerto " + port);

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> {
                try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                ) {
                    String request = in.readLine();
                    System.out.println("Nodo en " + port + " recibió: " + request);
                    Thread.sleep(1000); // simulamos procesamiento
                    out.println("Nodo " + port + " procesó: " + request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
