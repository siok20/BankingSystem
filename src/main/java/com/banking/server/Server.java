
package com.banking.server;

import com.banking.managedata.ManageData;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author siok
 */
public class Server {
    private final int MAX_CLIENTS = 10;
    private final int MAX_NODES = 5;    
    
    private String message;
    int nrcli = 0;
    int nodeCount = 0;
    public static final int SERVER_PORT = 8080;
    private OnMessageReceived messageListener = null;
    private volatile boolean running = false;
    
    ServerThread[] clients = new ServerThread[MAX_CLIENTS];
    Thread[] clientsThreads = new Thread[MAX_CLIENTS];
    
    PrintWriter mOut;
    BufferedReader in;
    ServerSocket serverSocket;
    
    Map<String,List<String>> nodes = new HashMap<>();
    
    //el constructor pide una interface OnMessageReceived
    public Server(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
        
        //Agregar el registro de los nodos 
        for(int i=0; i<MAX_NODES; i++){
            try {
                String nodo = "nodo" + i;
                String port = String.valueOf(8080 + i);
                nodes.put(nodo, Arrays.asList("localhost", port, "activo"));
                Files.createDirectories(Paths.get("src/main/data/nodo" + i));
                                
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        ManageData.generarDatosIniciales(100);
        
        ManageData.particionarDatos("cliente", 3, "src/main/data/original", "src/main/data/cliente");
        ManageData.particionarDatos("cuenta", 3, "src/main/data/original", "src/main/data/cuentas");
        
        ManageData.replicarDatos(5, 3);
            
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
