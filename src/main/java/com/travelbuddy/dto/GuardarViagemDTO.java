package com.travelbuddy.dto;

import lombok.Data;

@Data
public class GuardarViagemDTO {
    private String cidade;
    private String dataInicio;
    private String dataFim;
    private double lat;
    private double lon;

    // Hotel (opcional)
    private String hotelNome;
    private String hotelMorada;
    private double hotelPreco;
    private double hotelRating;
    private String hotelFoto;

    // Voo de ida (opcional)
    private String vooIdaOrigem;
    private String vooIdaDestino;
    private String vooIdaCompanhia;
    private String vooIdaHoraPartida;
    private String vooIdaHoraChegada;
    private double vooIdaPreco;
    private String vooIdaLogoUrl;
    private int vooIdaEscalas;
    private int vooIdaDuracaoMinutos;

    // Meteorologia (opcional)
    private Double meteoTemperaturaMedia;
    private Double meteoTemperaturaMin;
    private Double meteoTemperaturaMax;
    private String meteoDescricao;
    private Double meteoProbabilidadeChuva;
    private Double meteoVentoKmh;

    // Voo de regresso (opcional)
    private String vooRegressoOrigem;
    private String vooRegressoDestino;
    private String vooRegressoCompanhia;
    private String vooRegressoHoraPartida;
    private String vooRegressoHoraChegada;
    private double vooRegressoPreco;
    private String vooRegressoLogoUrl;
    private int vooRegressoEscalas;
    private int vooRegressoDuracaoMinutos;
}
