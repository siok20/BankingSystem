
package com.banking.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.Socket;


public class ServerThread extends Thread{
    
    private Socket client;
    private Server tcpserver;
    private int clientID;                 
    private boolean running = false;
    public PrintWriter mOut;
    public BufferedReader in;
    private Server.OnMessageReceived messageListener = null;
    private String message;
    ServerThread[] all_clients;

    public ServerThread(Socket client, Server tcpserver, int clientID ,ServerThread[] all_clients) {
        this.client = client;
        this.tcpserver = tcpserver;
        this.clientID = clientID;
        this.all_clients = all_clients;
    }
    
     public void trabajen(int cli){      
         mOut.println("TRABAJAMOS ["+cli+"]...");
    }
    
    public void run() {
        running = true;
        try {
            mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            messageListener = tcpserver.getMessageListener();
            System.out.println("TCP ServerC: Sent.");

            while (running) {
                try {
                    message = in.readLine();
                    if (message != null && messageListener != null) {
                        messageListener.messageReceived(message);
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("TCP Server: Error en readLine(): " + e.getMessage());
                    }
                    break; // Salir del bucle si el socket se cerró
                }
            }

        } catch (IOException e) {
            System.err.println("TCP ServerC: Error general: " + e.getMessage());
        } finally {
            close();
            System.out.println("Hilo del cliente " + clientID + " finalizado.");
        }
    }
    
    public void stopClient(){
        running = false;
        close();
    }
    
    public void sendMessage(String message){//funcion de trabajo
        if (mOut != null && !mOut.checkError()) {
            mOut.println( message);
            mOut.flush();
        }
    }    
    
    public void close(){
        try {
            running = false;

            if (in != null) {
                in.close();
            }

            if (mOut != null) {
                mOut.close();
            }

            if (client != null && !client.isClosed()) {
                client.close();
            }

            System.out.println("Cliente " + clientID + " desconectado correctamente");

        } catch (IOException e) {
            System.err.println("Error al cerrar recursos del cliente " + clientID + ": " + e.getMessage());
        }
    }
}
