
package com.banking.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


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
    
    String operacion;
    List<Integer> datos;
    
    private final File archivo;
    private final ReentrantLock lock = new ReentrantLock();
    
    public ServerThread(Socket client, Server tcpserver, int clientID ,ServerThread[] all_clients) {
        this.archivo = new File("src/main/data/original/transferencias.txt");
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
            System.out.println("Hilo del cliente " + clientID + " running");

            while (running) {
                try {
                    message = in.readLine();
                    //String[] parts;
                    if (message != null && messageListener != null) {
                        System.out.println("Mensaje desde el cliente id: " + clientID);
                        //messageListener.messageReceived(message);
                        /*parts = message.split(",");
                        
                        operacion = parts[0];
                        
                        datos = new ArrayList<Integer>();
                        
                        for (int i = 1; i < parts.length; i++) {
                            datos.add(Integer.parseInt(parts[i]));
                        }*/
                        
                        String mensaje = asignarNode(message);
                        
                        if(!mensaje.contains("?")){
                            mOut.println(mensaje);
                            continue;
                        }
                        
                        String[] partes = mensaje.split("\\?");
                        if (partes.length != 2) {
                            System.err.println("Formato inválido: " + mensaje);
                            return;
                        }
                        
                        String parte1 = partes[0].trim();
                        String parte2 = partes[1].trim();
                        
                        mOut.println(parte2);
                        
                        lock.lock();
                        try {
                            int numeroLinea = contarLineas() + 1;
                            String lineaNueva = numeroLinea + " | " + parte1;

                            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo, true))) {
                                writer.write(lineaNueva);
                                writer.newLine();
                            }
                        } catch (IOException e) {
                            System.err.println("Error escribiendo archivo: " + e.getMessage());
                        } finally {
                            lock.unlock();
                        }
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
    
    public String asignarNode(String mensaje) {
        Map<String, List<String>> nodes = tcpserver.nodes;

        for (Map.Entry<String, List<String>> entry : nodes.entrySet()) {
            String clave = entry.getKey();
            List<String> valores = entry.getValue();

            if (valores.size() >= 3 && valores.get(2).equalsIgnoreCase("activo")) {
                String host = valores.get(0);
                int port = Integer.parseInt(valores.get(1));

                // Cambiar el estado a "inactivo" temporalmente
                valores.set(2, "inactivo");
                System.out.println("Estado del nodo " + clave + " cambiado temporalmente a INACTIVO");

                try (
                    Socket socket = new Socket(host, port);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                ) {
                    System.out.println("Conectado a " + clave + " (" + host + ":" + port + ")");
                    out.println(mensaje);
                    String respuesta = in.readLine();

                    if ("REJECTED".equalsIgnoreCase(respuesta)) {
                        System.out.println("Nodo " + clave + " rechazó la solicitud.");
                        // Restaurar estado a "activo"
                        valores.set(2, "activo");
                        continue;
                    }

                    System.out.println("Respuesta de " + clave + ": " + respuesta);

                    // Restaurar estado a "activo" después de éxito
                    valores.set(2, "activo");
                    return respuesta;
                } catch (IOException e) {
                    System.err.println("No se pudo conectar con " + clave + ": " + e.getMessage());
                    // El nodo permanece inactivo para evitar nuevos intentos
                }
            }
        }

        return "NO_NODES_AVAILABLE";
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
    
    private int contarLineas() {
        int lineas = 0;
        if (!archivo.exists()) return 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            while (reader.readLine() != null) {
                lineas++;
            }
        } catch (IOException e) {
            System.err.println("Error contando líneas: " + e.getMessage());
        }
        return lineas;
    }
}
