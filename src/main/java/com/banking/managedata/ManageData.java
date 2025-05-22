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
    
   public static void replicarDatos(int MAX_NODES, int replicas) {
        replicarArchivosDesdeDirectorio("src/main/data/cliente", "cliente", MAX_NODES, replicas);
        replicarArchivosDesdeDirectorio("src/main/data/cuentas", "cuenta", MAX_NODES, replicas);
    }

    private static void replicarArchivosDesdeDirectorio(String directorio, String prefijoArchivo, int MAX_NODES, int replicas) {
        File dir = new File(directorio);
        File[] archivos = dir.listFiles();

        if (archivos == null || archivos.length == 0) {
            System.out.println("No se encontraron archivos en: " + directorio);
            return;
        }

        for (File archivo : archivos) {
            String nombre = archivo.getName();
            if (nombre.matches(prefijoArchivo + "\\.\\d+\\.\\d+\\.txt")) {
                replicarArchivoAReplicas(archivo, nombre, MAX_NODES, replicas);
            }
        }
    }

    private static void replicarArchivoAReplicas(File archivo, String nombre, int MAX_NODES, int replicas) {
        List<Integer> nodosAleatorios = getUniqueRandomNumbers(MAX_NODES, replicas);

        for (int nodoId : nodosAleatorios) {
            File carpetaNodo = new File("src/main/data/nodo" + nodoId);
            if (!carpetaNodo.exists()) carpetaNodo.mkdirs();

            Path destino = carpetaNodo.toPath().resolve(nombre);
            try {
                Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error copiando " + nombre + " a nodo" + nodoId + ": " + e.getMessage());
            }
        }
    }

    private static List<Integer> getUniqueRandomNumbers(int max, int cantidad) {
        Set<Integer> numeros = new HashSet<>();
        Random rnd = new Random();
        while (numeros.size() < cantidad) {
            numeros.add(rnd.nextInt(max)); // Asume nodos numerados desde 1 hasta MAX_NODES
        }
        return new ArrayList<>(numeros);
    }
}
