package com.travelbuddy.dto;

import lombok.Data;

@Data
public class VooDTO {
    private String origem;
    private String destino;
    private String companhia;
    private String partida;
    private String chegada;
    private String horaPartida;
    private String horaChegada;
    private int duracaoMinutos;
    private int escalas;
    private double preco;
    private String moeda;
    private String classeCabine;
    private String logoUrl;
    private String sentido;    // "IDA" ou "REGRESSO"
    private String nextToken;  // usado para pesquisar voos de regresso
}
