/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.model;

/**
 *
 * @author siok
 */
public class Cuenta {
    public int idCuenta;
    public int idCliente;
    public double saldo;
    public String tipoCuenta;

    public Cuenta(int idCuenta, int idCliente, double saldo, String tipoCuenta) {
        this.idCuenta = idCuenta;
        this.idCliente = idCliente;
        this.saldo = saldo;
        this.tipoCuenta = tipoCuenta;
    }
}
