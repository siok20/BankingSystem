
package com.banking.server;

import java.io.BufferedReader;
import java.io.IOException;
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
    private volatile boolean running = false;
    ServerThread[] clients = new ServerThread[10];
    Thread[] clientsThreads = new Thread[10];
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
        for (int i = 0; i < nrcli; i++) {
            clients[i].sendMessage(message);
            System.out.println("ENVIANDO A JUGADOR " + (i));
        }
    }
    
    public void run(){
        running = true;
        try{
            System.out.println("Socket Server: Connecting...");
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("Sucessfully");
            while(running){
                System.out.println("Server waiting a client...");
                Socket client = serverSocket.accept();
                
                if(!running) break;
                
                System.out.println("Cliente id: " + nrcli + " recibido");
                clients[nrcli] = new ServerThread(client,this,nrcli,clients);
                clientsThreads[nrcli] = new Thread(clients[nrcli]);
                clientsThreads[nrcli].start();
                System.out.println("Nuevo ciente conectado\n"+ (++nrcli) +" clientes conectados");
                
            }
            
        }catch( IOException e){
            
            if (running) {
                System.out.println("Error en la conexión: " + e.getMessage());
            }
            else System.out.println("Error "+e.getMessage());
            
        }finally{
            stop();
        }
    }
    public  ServerThread[] getClients(){
        return clients;
    } 

    void stop() {
        System.out.println("Apagando servidor...");
        running = false;
        
        try{
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
                System.out.println("serverSocket cerrado");
            }
        }catch(IOException e){
            System.out.println("Error cerrando ServerSocket: ");
        }
        
        for(int i=0; i<nrcli; i++){
            if(clients[i]!=null){
                clients[i].stopClient();
            }
            
            if(clientsThreads[i] != null){
                try{
                    clientsThreads[i].join(1000);
                }catch(InterruptedException e){
                    System.out.println("Error al cerrar el hilo del cliente " + i + ": " + e.getMessage());
                }
            }
        }
        
        
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
