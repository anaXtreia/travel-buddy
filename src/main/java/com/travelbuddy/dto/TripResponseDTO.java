package com.travelbuddy.dto;

import lombok.Data;
import java.util.List;

@Data
public class TripResponseDTO {
    private String cidade;
    private String dataInicio;
    private String dataFim;
    private CoordenadasDTO coordenadas;
    private MeteorologiaDTO meteorologia;
    private List<HotelDTO> hoteis;
    private List<VooDTO> voos;
}
