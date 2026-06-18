package com.travelbuddy.dto;

import lombok.Data;

@Data
public class HotelDTO {
    private String nome;
    private String morada;
    private double preco;
    private double rating;
    private int numAvaliacoes;
    private CoordenadasDTO coordenadas;
    private String website;
    private String telefone;
    private String foto;                    // URL da primeira foto (Booking.com)
    private double precoOriginal;           // preço riscado (strikethroughPrice), se existir
    private MeteorologiaDTO meteorologia;   // meteorologia específica do hotel
}
