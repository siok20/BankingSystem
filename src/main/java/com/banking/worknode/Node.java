/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.worknode;

import java.util.Scanner;

/**
 *
 * @author siok
 */
/*
public class Node {
    WorkNode wkNode;
    Scanner sc;
    public static void main(String[] args)  {
        Node node = new Node();
        node.iniciar();
    }
    void iniciar(){
       new Thread(
            new Runnable() {

                @Override
                public void run() {
                    wkNode = new WorkNode("127.0.0.1",
                        new WorkNode.OnMessageReceived(){
                            @Override
                            public void messageReceived(String message){
                                ClienteRecibe(message);
                            }
                        }
                    );
                    wkNode.run();                   
                }
            }
        ).start();
        //---------------------------
       
        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Cliente bandera 01");
        while( !salir.equals("s")){
            salir = sc.nextLine();
            ClienteEnvia(salir);
        }
        System.out.println("Cliente bandera 02");
    
    }
    void ClienteRecibe(String llego){
        System.out.println("CLINTE50 El mensaje::" + llego);

    }
    void ClienteEnvia(String envia){
        if (wkNode != null) {
            //wkNode.sendMessage(envia);
        }
    }
}
*/