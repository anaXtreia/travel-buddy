package com.travelbuddy.service;

import com.travelbuddy.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripSearchService {

    private final GeocodingService geocodingService;
    private final HotelService hotelService;
    private final FlightService flightService;
    private final WeatherService weatherService;

    public TripResponseDTO pesquisar(SearchRequestDTO request) {
        log.info("Iniciando pesquisa para: {}", request.getCidade());

        CoordenadasDTO coordenadas = geocodingService.geocodificar(request.getCidade());

        List<HotelDTO> hoteis = hotelService.pesquisarHoteis(
                request.getCidade(), request.getDataInicio(), request.getDataFim(),
                coordenadas.getLat(), coordenadas.getLon());

        MeteorologiaDTO meteorologia = weatherService.obterMeteorologiaPorCoordenadas(
                coordenadas.getLat(), coordenadas.getLon());

        List<VooDTO> voos = List.of();
        if (request.getAeroportoOrigem() != null && !request.getAeroportoOrigem().isBlank()
                && request.getAeroportoDestino() != null && !request.getAeroportoDestino().isBlank()) {
            voos = flightService.pesquisarVoos(
                    request.getAeroportoOrigem().toUpperCase().trim(),
                    request.getAeroportoDestino().toUpperCase().trim(),
                    request.getDataInicio(),
                    request.getDataFim());
        }

        TripResponseDTO resposta = new TripResponseDTO();
        resposta.setCidade(request.getCidade());
        resposta.setDataInicio(request.getDataInicio());
        resposta.setDataFim(request.getDataFim());
        resposta.setCoordenadas(coordenadas);
        resposta.setMeteorologia(meteorologia);
        resposta.setHoteis(hoteis);
        resposta.setVoos(voos);

        log.info("Pesquisa concluída: {} hotéis, {} voos", hoteis.size(), voos.size());
        return resposta;
    }
}
