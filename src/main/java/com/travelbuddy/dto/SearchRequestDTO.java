package com.travelbuddy.dto;

import lombok.Data;

@Data
public class SearchRequestDTO {
    private String cidade;
    private String dataInicio;
    private String dataFim;
    private String aeroportoOrigem;
    private String aeroportoDestino;
}
