
package com.banking.server;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author siok
 */
public class ServerController {
   Server tcpServer;
   Thread mainThread;
   Scanner sc;
   public static void main(String[] args) {
       ServerController server = new ServerController();
       server.iniciar();
   }
   void iniciar(){
        mainThread = new Thread(
            new Runnable() {
                @Override
                public void run() {
                      tcpServer = new Server(
                        new Server.OnMessageReceived(){
                            @Override
                            public void messageReceived(String message){
                                ServidorRecibe(message);
                            }
                        }
                    );
                    tcpServer.run();
                }
            }
        );
        
        mainThread.start();
        //-----------------
        
        sc = new Scanner(System.in);
        System.out.println("Inicio del Servidor Sistema Bancario");
        
        String input;
        
        while(true){
            input = sc.nextLine();
            
            if("EXIT_CODE".equalsIgnoreCase(input)){
                System.out.println("Cerrando sistema...");
                closeServer();
                break;
            }
            
            ServidorEnvia(input);
       }
        
       System.out.println("Servidor cerrado correctamente"); 
   
   }
   void ServidorRecibe(String llego){
       System.out.println("SERVIDOR40 El mensaje:" + llego);
   }
   
   void ServidorEnvia(String envia){
        if (tcpServer != null) {
            tcpServer.sendMessageTCPServer(envia);
        }
   }    

    private void closeServer() {
        
        try{
            if (tcpServer != null){
                tcpServer.stop();
            }
            if (sc != null) {
                sc.close();
            }
            if(mainThread != null && mainThread.isAlive()){
                mainThread.join(1000);
            }
            
        }catch(InterruptedException e){
            System.out.println("Error cerrando el servidor: " + e.getMessage());
        }
        
    }
}
