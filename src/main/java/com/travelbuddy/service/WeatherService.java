package com.travelbuddy.service;

import com.travelbuddy.dto.MeteorologiaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Serviço de Meteorologia: obtém previsão do tempo para coordenadas dadas.
 * Usa a API OpenWeatherMap (forecast de 5 dias).
 * Documentação: https://openweathermap.org/forecast5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${app.apis.openweather.key}")
    private String apiKey;

    @Value("${app.apis.openweather.base-url}")
    private String baseUrl;

    /**
     * Obtém previsão meteorológica para as coordenadas indicadas.
     * A API devolve previsões para os próximos 5 dias.
     */
    public MeteorologiaDTO obterMeteorologiaPorCoordenadas(double lat, double lon) {
        log.info("A obter meteorologia para lat={}, lon={}", lat, lon);

        Map response = webClient.get()
                .uri(baseUrl + "/forecast?lat={lat}&lon={lon}&appid={key}&units=metric&lang=pt&cnt=8",
                        lat, lon, apiKey)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return parseMeteorologiaResponse(response);
    }

    /**
     * Processa a resposta JSON da OpenWeatherMap e extrai os dados relevantes.
     */
    private MeteorologiaDTO parseMeteorologiaResponse(Map<?, ?> response) {
        MeteorologiaDTO dto = new MeteorologiaDTO();

        try {
            List<?> listItems = (List<?>) response.get("list");
            if (listItems == null || listItems.isEmpty()) {
                return dto;
            }

            // Usa a primeira previsão disponível como referência
            Map<?, ?> firstItem = (Map<?, ?>) listItems.getFirst();
            Map<?, ?> main = (Map<?, ?>) firstItem.get("main");
            Map<?, ?> wind = (Map<?, ?>) firstItem.get("wind");
            List<?> weather = (List<?>) firstItem.get("weather");

            dto.setTemperaturaMedia(((Number) main.get("temp")).doubleValue());
            dto.setTemperaturaMin(((Number) main.get("temp_min")).doubleValue());
            dto.setTemperaturaMax(((Number) main.get("temp_max")).doubleValue());
            dto.setVentoKmh(((Number) wind.get("speed")).doubleValue() * 3.6); // m/s → km/h

            if (weather != null && !weather.isEmpty()) {
                Map<?, ?> w = (Map<?, ?>) weather.getFirst();
                dto.setDescricao((String) w.get("description"));
                dto.setIcone((String) w.get("icon"));
            }

            // Probabilidade de chuva (pop = probability of precipitation)
            Object pop = firstItem.get("pop");
            if (pop != null) {
                dto.setProbabilidadeChuva(((Number) pop).doubleValue() * 100); // 0-1 → 0-100%
            }

        } catch (Exception e) {
            log.error("Erro ao processar resposta de meteorologia", e);
        }

        return dto;
    }
}
