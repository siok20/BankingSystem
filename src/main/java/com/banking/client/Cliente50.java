package com.banking.client;

import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Cliente50{
    TCPClient50 mTcpClient;
    Scanner sc;
    
    private boolean respuestaRecibida = false;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition respuestaCondition = lock.newCondition();

    public static void main(String[] args)  {
        Cliente50 objcli = new Cliente50();
        objcli.iniciar();
    }
    void iniciar(){
       new Thread(
            new Runnable() {

                @Override
                public void run() {
                    mTcpClient = new TCPClient50("127.0.0.1",
                        new TCPClient50.OnMessageReceived(){
                            @Override
                            public void messageReceived(String message){
                                ClienteRecibe(message);
                            }
                        }
                    );
                    mTcpClient.run();                   
                }
            }
        ).start();
        //---------------------------
       
        sc = new Scanner(System.in);
        System.out.println("Bienvenido al Sistema Bancario");
        
        String input, data;
        
        while(true){
            lock.lock();
            try {
                printMenu();
                input = sc.nextLine();

                if("EXIT_CODE".equalsIgnoreCase(input) || "3".equalsIgnoreCase(input)){
                    System.out.println("Saliendo del sistema...");
                    break;
                }

                if(!validateInput(input)){
                    System.out.println("");
                    System.out.println("Escriba una opcion correcta");
                    System.out.println("");
                    continue;
                } else {
                    data = getData(input);
                }

                respuestaRecibida = false;
                ClienteEnvia(data);

                // Espera la respuesta del servidor antes de continuar
                while (!respuestaRecibida) {
                    respuestaCondition.await();  // Bloquea el hilo principal
                }

            } catch (InterruptedException e) {
                System.err.println("Interrumpido: " + e.getMessage());
            } finally {
                lock.unlock();
            }
        }
        
        System.out.println("Cliente cierra acceso");
    
    }
    void ClienteRecibe(String llego){
        System.out.println("\n--- Respuesta del Servidor ---");
        System.out.println(llego);
        System.out.println("------------------------------\n");
        
        lock.lock();
        try {
            respuestaRecibida = true;
            respuestaCondition.signal();  // Despierta al hilo principal
        } finally {
            lock.unlock();
        }
    }
    void ClienteEnvia(String envia){
        if (mTcpClient != null) {
            mTcpClient.sendMessage(envia);
        }
    }
    void printMenu(){
        System.out.println("Elije el tipo de operacion (1 o 2)");
        System.out.println("1) Consultar Saldo");
        System.out.println("2) Transferir fondos");
        System.out.println("3) Salir");
        System.out.println("");
        System.out.print("Escribe tu eleccion: ");
    }
    
    boolean validateInput(String option){
        return "1".equalsIgnoreCase(option) || "2".equalsIgnoreCase(option);
    }
    
    public static boolean idValido(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean montoValido(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    String getData(String input){
        String operacion;
        StringBuilder sb= new StringBuilder();
        
        switch(input){
            case "1":
                System.out.println("OPERACION: CONSULTAR_SALDO");
                operacion = "CONSULTAR_SALDO";
                String id_cuenta;
                
                while(true){
                    System.out.print("Ingrese el id de su cuenta: ");
                    id_cuenta = sc.nextLine();
                    
                    if(idValido(id_cuenta)) break;
                    
                    System.out.println("Ingrese un id valido ");
                }
                
                sb.append(operacion).append(",").append(id_cuenta);
                break;
                
            case "2":
                System.out.println("OPERACION: TRANSFERIR_FONDOS");
                operacion = "TRANSFERIR_FONDOS";
                
                String id_cuenta_origen, id_cuenta_destino, monto; 
                
                while(true){
                    System.out.print("Ingrese el id de la cuenta origen: ");
                    id_cuenta_origen = sc.nextLine();
                    
                    if(idValido(id_cuenta_origen)) break;
                    
                    System.out.println("Ingrese un id valido ");
                }
                
                while(true){
                    System.out.print("Ingrese el id de la cuenta destino: ");
                    id_cuenta_destino = sc.nextLine();
                    
                    if(idValido(id_cuenta_destino)) break;
                    
                    System.out.println("Ingrese un id valido ");
                }
                
                while(true){
                    System.out.print("Ingrese el monto a transferir: ");
                    monto = sc.nextLine();
                    
                    if(montoValido(monto)) break;
                    
                    System.out.println("Ingrese un monto valido ");
                }
                sb.append(operacion).append(",").append(id_cuenta_origen).append(",").append(id_cuenta_destino).append(",").append(monto);
                break;
                
            default : 
                sb.append("ERROR");
                break;
        }
        
        return sb.toString();
    }
}
