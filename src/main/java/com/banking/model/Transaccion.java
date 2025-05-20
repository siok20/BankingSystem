/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.banking.model;

/**
 *
 * @author siok
 */
public class Transaccion {
    int id;
    int idOrigen;
    int idDestino;
    double monto;
    String fechaHora;
    String estado;

    public Transaccion(int id, int idOrigen, int idDestino, double monto, String fechaHora, String estado) {
        this.id = id;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.monto = monto;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }
}
