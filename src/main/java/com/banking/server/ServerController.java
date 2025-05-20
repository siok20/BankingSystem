
package com.banking.server;

import java.util.Scanner;

/**
 *
 * @author siok
 */
public class ServerController {
   Server tcpServer;
   Scanner sc;
   public static void main(String[] args) {
       ServerController server = new ServerController();
       server.iniciar();
   }
   void iniciar(){
       new Thread(
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
        ).start();
        //-----------------
        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Servidor bandera 01");
        while( !salir.equals("s")){
            salir = sc.nextLine();
            ServidorEnvia(salir);
       }
       System.out.println("Servidor bandera 02"); 
   
   }
   void ServidorRecibe(String llego){
       System.out.println("SERVIDOR40 El mensaje:" + llego);
   }
   void ServidorEnvia(String envia){
        if (tcpServer != null) {
            tcpServer.sendMessageTCPServer(envia);
        }
   }    
}
