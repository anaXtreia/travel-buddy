package com.travelbuddy.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "viagens")
public class Viagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String emailUtilizador;
    private String cidade;
    private String dataInicio;
    private String dataFim;
    private double lat;
    private double lon;

    // Hotel selecionado
    private String hotelNome;
    private String hotelMorada;
    private double hotelPreco;
    private double hotelRating;
    private String hotelFoto;

    // Voo de ida selecionado
    private String vooIdaOrigem;
    private String vooIdaDestino;
    private String vooIdaCompanhia;
    private String vooIdaHoraPartida;
    private String vooIdaHoraChegada;
    private double vooIdaPreco;
    private String vooIdaLogoUrl;
    private int vooIdaEscalas;
    private int vooIdaDuracaoMinutos;

    // Voo de regresso selecionado
    private String vooRegressoOrigem;
    private String vooRegressoDestino;
    private String vooRegressoCompanhia;
    private String vooRegressoHoraPartida;
    private String vooRegressoHoraChegada;
    private double vooRegressoPreco;
    private String vooRegressoLogoUrl;
    private int vooRegressoEscalas;
    private int vooRegressoDuracaoMinutos;

    // Meteorologia
    private Double meteoTemperaturaMedia;
    private Double meteoTemperaturaMin;
    private Double meteoTemperaturaMax;
    private String meteoDescricao;
    private Double meteoProbabilidadeChuva;
    private Double meteoVentoKmh;

    private LocalDateTime guardadoEm;

    @PrePersist
    public void prePersist() {
        guardadoEm = LocalDateTime.now();
    }
}
