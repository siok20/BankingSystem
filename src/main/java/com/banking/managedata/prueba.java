/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.managedata;

/**
 *
 * @author siok
 */
public class prueba {
    public static void main(String[] args){
        ManageData.generarDatosIniciales(10);
        
        ManageData.particionarDatos("cliente", 3, "src/main/data/original", "src/main/data/cliente");
        ManageData.particionarDatos("cuenta", 3, "src/main/data/original", "src/main/data/cuentas");

    }
}
