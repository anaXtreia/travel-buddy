package com.travelbuddy.service;

import com.travelbuddy.dto.CoordenadasDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Serviço de Geocoding: converte nome de cidade em coordenadas (lat/lon).
 * Usa a API Geoapify Geocoding.
 *
 * Documentação: https://apidocs.geoapify.com/docs/geocoding/
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final WebClient webClient;

    @Value("${app.apis.geoapify.key}")
    private String apiKey;

    @Value("${app.apis.geoapify.base-url}")
    private String baseUrl;

    /**
     * Recebe o nome de uma cidade e devolve as suas coordenadas.
     * Exemplo: "Punta Cana" → { lat: 18.58, lon: -68.40 }
     */
    public CoordenadasDTO geocodificar(String cidade) {
        log.info("A geocodificar cidade: {}", cidade);

        // Chama a API Geoapify
        Map response = webClient.get()
                .uri(baseUrl + "/geocode/search?text={cidade}&apiKey={key}&lang=pt&limit=1",
                        cidade, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // block() converte chamada assíncrona em síncrona

        // Extrai coordenadas da resposta JSON
        try {
            var features = (java.util.List<?>) response.get("features");
            if (features == null || features.isEmpty()) {
                throw new RuntimeException("Cidade não encontrada: " + cidade);
            }
            var feature = (Map<?, ?>) features.get(0);
            var geometry = (Map<?, ?>) feature.get("geometry");
            var coordinates = (java.util.List<?>) geometry.get("coordinates");

            CoordenadasDTO dto = new CoordenadasDTO();
            dto.setLon(((Number) coordinates.get(0)).doubleValue());
            dto.setLat(((Number) coordinates.get(1)).doubleValue());

            log.info("Coordenadas obtidas: lat={}, lon={}", dto.getLat(), dto.getLon());
            return dto;

        } catch (Exception e) {
            log.error("Erro ao processar resposta de geocoding para: {}", cidade, e);
            throw new RuntimeException("Erro ao geocodificar cidade: " + cidade);
        }
    }
}
