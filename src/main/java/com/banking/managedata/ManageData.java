/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.managedata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author siok
 */
public class ManageData {
    
    private static final Random random = new Random();
    
    public static void generarDatosIniciales(int cantidad) {
        String clientePath = "src/main/data/original/clientes.txt";
        String cuentaPath = "src/main/data/original/cuentas.txt";
        String transferenciasPath = "src/main/data/original/transferencias.txt";

        try {
            // Crea carpeta si no existe
            Files.createDirectories(Paths.get("src/main/data"));
            Files.createDirectories(Paths.get("src/main/data/original"));

            try (PrintWriter pwCliente = new PrintWriter(new FileWriter(clientePath));
                 PrintWriter pwCuenta = new PrintWriter(new FileWriter(cuentaPath))) {

                for (int i = 1; i <= cantidad; i++) {
                    String nombre = "Cliente_" + i;
                    String email = "cliente" + i + "@banco.com";
                    String telefono = "9" + (100000000 + random.nextInt(899999999));
                    pwCliente.println(i + " | " + nombre + " | " + email + " | " + telefono);

                    int tipo = random.nextInt(2);
                    double saldo = Math.round((random.nextDouble() * 10000) * 100.0) / 100.0;
                    String tipoCuenta = (tipo == 0) ? "Ahorros" : "Corriente";
                    pwCuenta.println((100 + i) + " | " + i + " | " + saldo + " | " + tipoCuenta);
                }

                System.out.println("Datos generados con exito.");
                
                File archivoTransferencias = new File(transferenciasPath);
                if (archivoTransferencias.createNewFile()) {
                    System.out.println("Archivo de transferencias creado.");
                } else {
                    System.out.println("Archivo de transferencias ya existía.");
                }

            }
        } catch (IOException e) {
            System.err.println("Error al generar datos iniciales: " + e.getMessage());
        }
    }
    
    public static void particionarDatos(String tipo, int particiones, String carpetaOrigen, String carpetaDestino) {
        String archivo = carpetaOrigen + "/" + tipo + "s.txt";

        try {
            List<String> lineas = Files.readAllLines(Paths.get(archivo));
            int total = lineas.size();
            int porParticion = (int) Math.ceil((double) total / particiones);

            // Crear carpeta destino si no existe
            Files.createDirectories(Paths.get(carpetaDestino));

            for (int i = 0; i < particiones; i++) {
                int inicio = i * porParticion;
                int fin = Math.min(inicio + porParticion, total);

                List<String> subLista = lineas.subList(inicio, fin);
                String nombreArchivo = carpetaDestino + "/" + tipo + "." + (i + 1) + ".1.txt";
                String replica2 = carpetaDestino + "/" + tipo + "." + (i + 1) + ".2.txt";
                String replica3 = carpetaDestino + "/" + tipo + "." + (i + 1) + ".3.txt";
                Files.write(Paths.get(nombreArchivo), subLista);
                Files.write(Paths.get(replica2), subLista);
                Files.write(Paths.get(replica3), subLista);

                System.out.println(nombreArchivo + " creado con " + subLista.size() + " registros.");
            }

        } catch (IOException e) {
            System.err.println("Error al particionar " + tipo + ": " + e.getMessage());
        }
    }
    
    public static void replicarDatos(int MAX_NODES, int replicas){
        File clienteDir = new File("src/main/data/cliente");
        File[] files = clienteDir.listFiles();

        if(files == null){
            System.out.println("no se encontraron clientes");
            return;
        }
        
        for(File file:files){
            String name = file.getName();

            if (name.matches("cliente\\.\\d+\\.\\d+\\.txt")){
                String[] partes = name.replace(".txt", "").replace("cliente.", "").split("\\.");
                int numPart = Integer.parseInt(partes[0]); 
                int numRep = Integer.parseInt(partes[1]);
                
                
                List<Integer> randomNodes = getThreeRandomNumbers(MAX_NODES);
                int nodoDestino = randomNodes.get(0);
                int nodoDestino1 = randomNodes.get(1);
                int nodoDestino2 = randomNodes.get(2);

                File carpetaNodo = new File("src/main/data/nodo" + nodoDestino);
                File carpetaNodo1 = new File("src/main/data/nodo" + nodoDestino1);
                File carpetaNodo2 = new File("src/main/data/nodo" + nodoDestino2);
                if (!carpetaNodo.exists()) carpetaNodo.mkdirs();

                Path destino = carpetaNodo.toPath().resolve(name);
                Path destino1 = carpetaNodo1.toPath().resolve(name);
                Path destino2 = carpetaNodo2.toPath().resolve(name);
                try {
                    Files.copy(file.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(file.toPath(), destino1, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(file.toPath(), destino2, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Error copiando " + name + ": " + e.getMessage());
                }
            }
        }
        
        File cuentaDir = new File("src/main/data/cuentas");
        files = cuentaDir.listFiles();

        if(files == null){
            System.out.println("no se encontraron cuentas");
            return;
        }
        
        for(File file:files){
            String name = file.getName();

            if (name.matches("cuenta\\.\\d+\\.\\d+\\.txt")){
                String[] partes = name.replace(".txt", "").replace("cuenta.", "").split("\\.");
                int numPart = Integer.parseInt(partes[0]); 
                int numRep = Integer.parseInt(partes[1]);
                
                
                List<Integer> randomNodes = getThreeRandomNumbers(MAX_NODES);
                int nodoDestino = randomNodes.get(0);
                int nodoDestino1 = randomNodes.get(1);
                int nodoDestino2 = randomNodes.get(2);

                File carpetaNodo = new File("src/main/data/nodo" + nodoDestino);
                File carpetaNodo1 = new File("src/main/data/nodo" + nodoDestino1);
                File carpetaNodo2 = new File("src/main/data/nodo" + nodoDestino2);
                if (!carpetaNodo.exists()) carpetaNodo.mkdirs();

                Path destino = carpetaNodo.toPath().resolve(name);
                Path destino1 = carpetaNodo1.toPath().resolve(name);
                Path destino2 = carpetaNodo2.toPath().resolve(name);
                try {
                    Files.copy(file.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(file.toPath(), destino1, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(file.toPath(), destino2, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Error copiando " + name + ": " + e.getMessage());
                }
            }
        }
    }
    
        public static List<Integer> getThreeRandomNumbers(int N) {
        if (N < 2) {
            throw new IllegalArgumentException("N debe ser al menos 2 para obtener 3 números distintos.");
        }

        Random rand = new Random();
        Set<Integer> uniqueNumbers = new HashSet<>();

        while (uniqueNumbers.size() < 3) {
            uniqueNumbers.add(rand.nextInt(N)); // 0 a N inclusive
        }
        //System.out.println(uniqueNumbers);
        return new ArrayList<>(uniqueNumbers);
    }
}
