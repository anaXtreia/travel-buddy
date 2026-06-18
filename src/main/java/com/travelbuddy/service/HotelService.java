package com.travelbuddy.service;

import com.travelbuddy.dto.CoordenadasDTO;
import com.travelbuddy.dto.HotelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final WebClient webClient;

    @Value("${app.apis.rapidapi.key}")
    private String rapidApiKey;

    @Value("${app.apis.booking.host}")
    private String bookingHost;

    @Value("${app.apis.booking.base-url}")
    private String baseUrl;

    public List<HotelDTO> pesquisarHoteis(String cidade, String dataInicio, String dataFim,
                                          double lat, double lon) {
        log.info("A pesquisar hotéis via Booking.com para: {}", cidade);
        try {
            String destId = resolverDestId(cidade);
            if (destId == null) {
                log.warn("dest_id não encontrado para '{}', a usar mock", cidade);
                return mockHoteis(cidade, lat, lon);
            }

            List<HotelDTO> hoteis = pesquisarHoteisApi(destId, dataInicio, dataFim, lat, lon);
            if (hoteis.isEmpty()) {
                log.warn("Booking.com não devolveu hotéis para '{}', a usar mock", cidade);
                return mockHoteis(cidade, lat, lon);
            }

            log.info("Hotéis encontrados via Booking.com: {}", hoteis.size());
            return hoteis;

        } catch (Exception e) {
            log.error("Erro ao pesquisar hotéis na Booking.com, a usar mock: {}", e.getMessage());
            return mockHoteis(cidade, lat, lon);
        }
    }

    private String resolverDestId(String cidade) {
        Map<?, ?> response = webClient.get()
                .uri(baseUrl + "/api/v1/hotels/searchDestination?query={q}", cidade)
                .header("x-rapidapi-key", rapidApiKey)
                .header("x-rapidapi-host", bookingHost)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) return null;

        List<?> data = (List<?>) response.get("data");
        if (data == null || data.isEmpty()) return null;

        // Preferir resultado do tipo city
        for (Object item : data) {
            Map<?, ?> m = (Map<?, ?>) item;
            if ("city".equals(m.get("search_type"))) {
                Object id = m.get("dest_id");
                return id != null ? id.toString() : null;
            }
        }

        // Fallback: primeiro resultado
        Map<?, ?> primeiro = (Map<?, ?>) data.getFirst();
        Object id = primeiro.get("dest_id");
        return id != null ? id.toString() : null;
    }

    private List<HotelDTO> pesquisarHoteisApi(String destId, String dataInicio, String dataFim,
                                               double cidadeLat, double cidadeLon) {
        String uri = baseUrl + "/api/v1/hotels/searchHotels" +
                "?dest_id=" + destId +
                "&search_type=CITY" +
                "&arrival_date=" + dataInicio +
                "&departure_date=" + dataFim +
                "&adults=1&room_qty=1" +
                "&units=metric&temperature_unit=c" +
                "&languagecode=pt-pt&currency_code=EUR&location=PT";

        Map<?, ?> response = webClient.get()
                .uri(uri)
                .header("x-rapidapi-key", rapidApiKey)
                .header("x-rapidapi-host", bookingHost)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) return List.of();

        Map<?, ?> data = (Map<?, ?>) response.get("data");
        if (data == null) return List.of();

        List<?> hoteis = (List<?>) data.get("hotels");
        if (hoteis == null) return List.of();

        long noites = ChronoUnit.DAYS.between(LocalDate.parse(dataInicio), LocalDate.parse(dataFim));
        if (noites <= 0) noites = 1;

        List<HotelDTO> resultado = new ArrayList<>();
        for (Object item : hoteis) {
            HotelDTO dto = parseHotel((Map<?, ?>) item, cidadeLat, cidadeLon, noites);
            if (dto != null) resultado.add(dto);
            if (resultado.size() >= 10) break;
        }
        return resultado;
    }

    private HotelDTO parseHotel(Map<?, ?> item, double cidadeLat, double cidadeLon, long noites) {
        try {
            Map<?, ?> property = (Map<?, ?>) item.get("property");
            if (property == null) return null;

            HotelDTO h = new HotelDTO();

            h.setNome((String) property.get("name"));

            // Coordenadas
            CoordenadasDTO coords = new CoordenadasDTO();
            Object lat = property.get("latitude");
            Object lon = property.get("longitude");
            coords.setLat(lat != null ? ((Number) lat).doubleValue() : cidadeLat);
            coords.setLon(lon != null ? ((Number) lon).doubleValue() : cidadeLon);
            h.setCoordenadas(coords);

            // Foto
            List<?> fotos = (List<?>) property.get("photoUrls");
            if (fotos != null && !fotos.isEmpty()) {
                h.setFoto((String) fotos.getFirst());
            }

            // Rating
            Object reviewScore = property.get("reviewScore");
            if (reviewScore != null) h.setRating(((Number) reviewScore).doubleValue());

            Object reviewCount = property.get("reviewCount");
            if (reviewCount != null) h.setNumAvaliacoes(((Number) reviewCount).intValue());

            // Preço por noite (API devolve total da estadia)
            Map<?, ?> priceBreakdown = (Map<?, ?>) property.get("priceBreakdown");
            if (priceBreakdown != null) {
                Map<?, ?> grossPrice = (Map<?, ?>) priceBreakdown.get("grossPrice");
                if (grossPrice != null) {
                    Object valor = grossPrice.get("value");
                    if (valor != null) h.setPreco(((Number) valor).doubleValue() / noites);
                }
                Map<?, ?> strikethrough = (Map<?, ?>) priceBreakdown.get("strikethroughPrice");
                if (strikethrough != null) {
                    Object valor = strikethrough.get("value");
                    if (valor != null) h.setPrecoOriginal(((Number) valor).doubleValue() / noites);
                }
            }

            // Morada (nem sempre disponível)
            Object address = property.get("wishlistName");
            if (address != null) h.setMorada(address.toString());

            return h;

        } catch (Exception e) {
            log.warn("Erro ao fazer parse de hotel: {}", e.getMessage());
            return null;
        }
    }

    // Fallback mock usado quando a API falha ou está em rate limit
    private List<HotelDTO> mockHoteis(String cidade, double lat, double lon) {
        log.info("A gerar hotéis mock para: {} ({}, {})", cidade, lat, lon);

        double[][] offsets = {
            { 0.000,  0.000}, { 0.008,  0.012}, {-0.006,  0.015},
            { 0.015, -0.008}, {-0.012, -0.010}, { 0.010,  0.018},
            {-0.018,  0.005}, { 0.005, -0.018}, { 0.018,  0.003}, {-0.003,  0.020}
        };
        String[][] templates = {
            {"Hotel Central %s", "Rua Principal, 1"},
            {"Grand Hotel %s", "Praça Central, 10"},
            {"Ibis %s Centro", "Av. da República, 45"},
            {"Novotel %s", "Rua do Comércio, 23"},
            {"Mercure %s", "Largo da Sé, 5"},
            {"%s Boutique Hotel", "Rua das Flores, 88"},
            {"Holiday Inn %s", "Av. da Liberdade, 200"},
            {"Best Western %s", "Rua do Castelo, 12"},
            {"Hilton Garden Inn %s", "Av. Europa, 55"},
            {"Marriott %s", "Rua Nova, 30"},
        };
        double[] precos   = {79, 95, 115, 142, 189, 210, 245, 275, 320, 68};
        double[] originais = {0,   0, 130,   0, 210,   0, 290, 310, 380, 85};
        double[] ratings  = {7.5, 8.9, 8.4, 8.2, 8.7, 8.6, 9.0, 8.6, 9.1, 7.8};
        int[]    avals    = {4312, 2108, 1543, 1876, 2341, 1654, 1102, 987, 742, 3210};
        String[] fotos = {
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707778.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707779.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707780.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707781.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707782.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707783.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707784.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707785.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707786.jpg",
            "https://cf.bstatic.com/xdata/images/hotel/max1024x768/261707787.jpg",
        };

        long seed = cidade.toLowerCase().chars().asLongStream().sum();
        Random rng = new Random(seed);
        int[] ordem = rng.ints(0, 10).distinct().limit(10).toArray();
        String cidadeCapitalizada = Character.toUpperCase(cidade.charAt(0)) + cidade.substring(1).toLowerCase();

        HotelDTO[] hoteis = new HotelDTO[10];
        for (int i = 0; i < 10; i++) {
            int idx = ordem[i];
            HotelDTO h = new HotelDTO();
            h.setNome(String.format(templates[idx][0], cidadeCapitalizada));
            h.setMorada(templates[idx][1]);
            h.setPreco(precos[idx]);
            h.setPrecoOriginal(originais[idx]);
            h.setRating(ratings[idx]);
            h.setNumAvaliacoes(avals[idx]);
            h.setFoto(fotos[idx]);
            CoordenadasDTO coords = new CoordenadasDTO();
            coords.setLat(lat + offsets[i][0]);
            coords.setLon(lon + offsets[i][1]);
            h.setCoordenadas(coords);
            hoteis[i] = h;
        }
        return List.of(hoteis);
    }
}
