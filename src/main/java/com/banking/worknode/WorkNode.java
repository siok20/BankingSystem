/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.worknode;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("Conexion con el servidor");
            new Thread(() -> handleClient(socket, port)).start();
        }
    }

    private static void handleClient(Socket socket, int port) {
        int id = port % 8081;
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String operacion;
            List<Integer> datos;
            String request = in.readLine();
            System.out.println("Nodo en puerto " + port + " recibió: " + request);
            
            String[] parts;
            
            parts = request.split(",");
                        
            operacion = parts[0];

            datos = new ArrayList<>();

            for (int i = 1; i < parts.length; i++) {
                datos.add(Integer.valueOf(parts[i]));
            }

            if (reject(operacion, datos, id)) {
                out.println("REJECTED");
                socket.close();
                System.out.println("Nodo en puerto " + port + " rechazó la conexión.");
                return;
            }
            String result = processOperation(operacion, datos, id);
            out.println(result);
            Thread.sleep(1000); // Simula procesamiento
            out.println("Nodo " + port + " procesó: " + request);

        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }
    
    private static String processOperation(String operacion, List<Integer> datos, int id_socket) {
        String carpetaPath = "src/main/data/nodo" + id_socket;
        
        System.out.println(operacion);
        
        switch(operacion){
            case "CONSULTAR_SALDO" -> {
                int id = datos.get(0);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(carpetaPath), "cuenta.*.txt")) {
                    for (Path path : stream) {
                        List<String> lineas = Files.readAllLines(path);

                        for (String linea : lineas) {
                            // Divide por "|", quita espacios y toma el primer valor
                            String[] partes = linea.split("\\|");
                            if (partes.length > 0) {
                                String numeroCuentaStr = partes[0].trim();

                                if (numeroCuentaStr.isEmpty()) continue;

                                int numeroCuenta;
                                try {
                                    numeroCuenta = Integer.parseInt(numeroCuentaStr);
                                } catch (NumberFormatException e) {
                                    continue; // Línea malformada, la salteamos
                                }

                                if (numeroCuenta == id) {
                                    // ¡Encontramos la cuenta! Retornar saldo por ejemplo
                                    String saldo = partes.length >= 3 ? partes[2].trim() : "Desconocido";
                                    return "SALDO: " + saldo;
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error al leer archivos de cuenta: " + e.getMessage());
                }
            }    
            case "TRANSFERIR_FONDOS" -> {
                int id1 = datos.get(0);
                int id2 = datos.get(1);
                int monto = datos.get(2);
                
                if (monto <= 0) {
                    return "REJECTED: Monto inválido";
                }
                
                if(id1 == id2) return "REJECTED: Cuentas invalidas" ;
                
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(carpetaPath), "cuenta.*.txt")) {
                    Path pathCuenta1 = null;
                    Path pathCuenta2 = null;
                    List<String> lineasCuenta1 = null;
                    List<String> lineasCuenta2 = null;
                    int saldo1 = -1, saldo2 = -1;
                    int indexLineaCuenta1 = -1, indexLineaCuenta2 = -1;

                    // Buscar ambas cuentas y cargar sus líneas
                    for (Path path : stream) {
                        List<String> lineas = Files.readAllLines(path);
                        for (int i = 0; i < lineas.size(); i++) {
                            String linea = lineas.get(i);
                            String[] partes = linea.split("\\|");
                            if (partes.length >= 3) {
                                int numeroCuenta = Integer.parseInt(partes[0].trim());
                                int saldoActual = (int) Double.parseDouble(partes[2].trim());

                                if (numeroCuenta == id1) {
                                    pathCuenta1 = path;
                                    lineasCuenta1 = lineas;
                                    saldo1 = saldoActual;
                                    indexLineaCuenta1 = i;
                                } else if (numeroCuenta == id2) {
                                    pathCuenta2 = path;
                                    lineasCuenta2 = lineas;
                                    saldo2 = saldoActual;
                                    indexLineaCuenta2 = i;
                                }
                            }
                        }
                    }

                    if (saldo1 == -1 || saldo2 == -1) {
                        return "REJECTED: Cuenta no encontrada";
                    }
                    if (saldo1 < monto) {
                        return "REJECTED: Saldo insuficiente";
                    }

                    // Actualizar saldos
                    saldo1 -= monto;
                    saldo2 += monto;

                    // Actualizar las líneas en memoria
                    String[] partesCuenta1 = lineasCuenta1.get(indexLineaCuenta1).split("\\|");
                    partesCuenta1[2] = String.valueOf(saldo1);
                    lineasCuenta1.set(indexLineaCuenta1, String.join(" | ", partesCuenta1));

                    String[] partesCuenta2 = lineasCuenta2.get(indexLineaCuenta2).split("\\|");
                    partesCuenta2[2] = String.valueOf(saldo2);
                    lineasCuenta2.set(indexLineaCuenta2, String.join(" | ", partesCuenta2));

                    // Escribir de vuelta los archivos con las líneas actualizadas
                    Files.write(pathCuenta1, lineasCuenta1);
                    if (!pathCuenta2.equals(pathCuenta1)) {
                        Files.write(pathCuenta2, lineasCuenta2);
                    }
                    
                    StringBuilder sb = new StringBuilder();
                    
                    sb.append(id1).append(" | ").append(id2).append(" | ").append(monto).append(" | ").append(obtenerFechaHoraActual());

                    return sb.toString() + "?SUCCESS: Transferencia realizada";
                } catch (IOException e) {
                    return "ERROR: " + e.getMessage();
                }
            }
                
            default -> {
            }
        }
        return "ERROR";
        
        
        
    }

    private static boolean reject(String request, List<Integer> valores, int id_socket) {
        List<Integer> ids = new ArrayList<>();
        switch(request){
            case "CONSULTAR_SALDO" -> ids.add(valores.get(0));
                
            case "TRANSFERIR_FONDOS" -> {
                ids.add(valores.get(0));
                ids.add(valores.get(1));
                
                if(valores.get(2) <= 0){
                    return true;
                }
            }
                
            default -> {
            }
        }
        
        String carpetaPath = "src/main/data/nodo" + id_socket;
        int num = 0;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(carpetaPath), "cuenta.*.txt")) {
            for (Path path : stream) {
                List<String> lineas = Files.readAllLines(path);

                for (String linea : lineas) {
                    // Divide por "|", quita espacios y toma el primer valor
                    String[] partes = linea.split("\\|");
                    if (partes.length > 0) {
                        String numeroCuenta = partes[0].trim();
                        if (numeroCuenta.isEmpty()) {
                            continue;
                        }
                        
                        int cuentaInt;
                        try {
                            cuentaInt = Integer.parseInt(numeroCuenta);
                        } catch (NumberFormatException e) {
                            continue; // Línea malformada, ignorar
                        }
                        
                        if(ids.contains(cuentaInt)){
                            num++;
                        }
                        
                        if(num == ids.size()) return false;
                        
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivos de cuenta: " + e.getMessage());
        }finally{
            return false;
        }

    }
    
    public static String obtenerFechaHoraActual() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ahora.format(formatter);
    }
}
