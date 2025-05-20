
package com.banking.server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;



/**
 *
 * @author siok
 */
public class Server {
    private String message;
    int nrcli = 0;
    public static final int SERVER_PORT = 8080;
    private OnMessageReceived messageListener = null;
    private boolean running = false;
    ServerThread[] clients = new ServerThread[10];
    PrintWriter mOut;
    BufferedReader in;
    ServerSocket serverSocket;
    //el constructor pide una interface OnMessageReceived
    public Server(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    public OnMessageReceived getMessageListener(){
        return this.messageListener;
    }
    public void sendMessageTCPServer(String message){
        for (int i = 1; i <= nrcli; i++) {
            clients[i].sendMessage(message);
            System.out.println("ENVIANDO A JUGADOR " + (i));
        }
    }
    
    public void run(){
        running = true;
        try{
            System.out.println("TCP Server"+"S : Connecting...");
            serverSocket = new ServerSocket(SERVER_PORT);
            
            while(running){
                Socket client = serverSocket.accept();
                System.out.println("TCP Server"+"S: Receiving...");
                nrcli++;
                System.out.println("Engendrado " + nrcli);
                clients[nrcli] = new ServerThread(client,this,nrcli,clients);
                Thread t = new Thread(clients[nrcli]);
                t.start();
                System.out.println("Nuevo conectado:"+ nrcli+" jugadores conectados");
                
            }
            
        }catch( Exception e){
            System.out.println("Error"+e.getMessage());
        }finally{

        }
    }
    public  ServerThread[] getClients(){
        return clients;
    } 

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
