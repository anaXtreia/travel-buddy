package com.travelbuddy.dto;

import lombok.Data;

@Data
public class MeteorologiaDTO {
    private double temperaturaMedia;
    private double temperaturaMin;
    private double temperaturaMax;
    private String descricao;           // ex: "céu limpo"
    private double probabilidadeChuva;
    private double ventoKmh;
    private String icone;               // código do ícone OpenWeather
}
