package com.travelbuddy.service;

import com.travelbuddy.dto.VooDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {

    private final WebClient webClient;

    @Value("${app.apis.rapidapi.key}")
    private String rapidApiKey;

    @Value("${app.apis.googleflights.host}")
    private String rapidApiHost;

    @Value("${app.apis.googleflights.base-url}")
    private String baseUrl;

    public List<VooDTO> pesquisarVoos(String origem, String destino,
                                      String dataIda, String dataVolta) {
        log.info("A pesquisar voos de ida: {} → {} em {}", origem, destino, dataIda);
        try {
            String uri = baseUrl + "/api/v1/searchFlights" +
                    "?departure_id=" + origem +
                    "&arrival_id=" + destino +
                    "&outbound_date=" + dataIda +
                    (dataVolta != null && !dataVolta.isBlank() ? "&return_date=" + dataVolta : "") +
                    "&travel_class=ECONOMY" +
                    "&adults=1" +
                    "&currency=EUR" +
                    "&language_code=pt-PT" +
                    "&country_code=PT";

            Map<?, ?> response = chamarApi(uri);
            if (response == null) return List.of();

            List<VooDTO> voos = parseOfertas(response, "IDA", true);
            log.info("Voos de ida encontrados: {}", voos.size());
            return voos;

        } catch (Exception e) {
            log.error("Erro ao pesquisar voos: {}", e.getMessage());
            return List.of();
        }
    }

    public List<VooDTO> pesquisarVoosRegresso(String nextToken) {
        log.info("A pesquisar voos de regresso com next_token");
        try {
            String uri = baseUrl + "/api/v1/getNextFlights?next_token=" +
                    java.net.URLEncoder.encode(nextToken, java.nio.charset.StandardCharsets.UTF_8);
            Map<?, ?> response = chamarApi(uri);
            if (response == null) return List.of();

            List<VooDTO> voos = parseOfertas(response, "REGRESSO", false);
            log.info("Voos de regresso encontrados: {}", voos.size());
            return voos;

        } catch (Exception e) {
            log.error("Erro ao pesquisar voos de regresso: {}", e.getMessage());
            return List.of();
        }
    }

    private Map<?, ?> chamarApi(String uri) {
        try {
            Map<?, ?> response = webClient.get()
                    .uri(uri)
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", rapidApiHost)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            clientResponse ->
                        clientResponse.bodyToMono(String.class).map(body -> {
                            log.error("Erro HTTP {} da API Google Flights: {}", clientResponse.statusCode(), body);
                            return new RuntimeException("API Google Flights erro " + clientResponse.statusCode());
                        })
                    )
                    .bodyToMono(Map.class)
                    .block();
            if (response == null) log.warn("Resposta nula da API Google Flights para: {}", uri);
            return response;
        } catch (Exception e) {
            log.error("Erro HTTP ao chamar API: {}", e.getMessage());
            return null;
        }
    }

    private List<?> extrairOfertas(Map<?, ?> data) {
        List<Object> lista = new ArrayList<>();
        Object itiObj = data.get("itineraries");
        if (itiObj instanceof Map<?, ?> itineraries) {
            List<?> top   = (List<?>) itineraries.get("topFlights");
            List<?> other = (List<?>) itineraries.get("otherFlights");
            if (top   != null) lista.addAll(top);
            if (other != null) lista.addAll(other);
        }
        return lista;
    }

    private List<VooDTO> parseOfertas(Map<?, ?> response, String sentido, boolean guardarToken) {
        List<VooDTO> voos = new ArrayList<>();

        Object statusObj = response.get("status");
        boolean statusOk = Boolean.TRUE.equals(statusObj) || "true".equalsIgnoreCase(String.valueOf(statusObj));
        if (!statusOk) {
            log.warn("API Google Flights status não OK: {}", statusObj);
            return voos;
        }

        try {
            Object dataObj = response.get("data");
            if (dataObj == null) return voos;

            List<?> todasOfertas;
            if (dataObj instanceof Map<?, ?> data) {
                todasOfertas = extrairOfertas(data);
            } else if (dataObj instanceof List<?> lista) {
                todasOfertas = lista;
            } else {
                return voos;
            }

            if (todasOfertas.isEmpty()) {
                log.warn("Nenhuma oferta ({}) na resposta", sentido);
                return voos;
            }

            for (Object ofertaObj : todasOfertas) {
                if (!(ofertaObj instanceof Map<?, ?> oferta)) continue;

                Object precoObj = oferta.get("price");
                if (!(precoObj instanceof Number)) continue;

                List<?> segmentos = (List<?>) oferta.get("flights");
                if (segmentos == null || segmentos.isEmpty()) continue;


                Map<?, ?> primeiro = (Map<?, ?>) segmentos.getFirst();
                Map<?, ?> ultimo   = (Map<?, ?>) segmentos.getLast();

                Map<?, ?> aeroportoPartida = (Map<?, ?>) primeiro.get("departure_airport");
                Map<?, ?> aeroportoChegada = (Map<?, ?>) ultimo.get("arrival_airport");

                String iataOrigem  = coalesce(getString(aeroportoPartida, "airport_code"), getString(aeroportoPartida, "id"));
                String iataDestino = coalesce(getString(aeroportoChegada, "airport_code"), getString(aeroportoChegada, "id"));

                String partida = getString(aeroportoPartida, "time");
                String chegada = getString(aeroportoChegada, "time");

                VooDTO voo = new VooDTO();
                voo.setOrigem(iataOrigem);
                voo.setDestino(iataDestino);
                voo.setSentido(sentido);
                voo.setPartida(partida);
                voo.setChegada(chegada);
                voo.setHoraPartida(extrairHora(partida));
                voo.setHoraChegada(extrairHora(chegada));
                voo.setCompanhia(getString(primeiro, "airline"));
                voo.setEscalas(segmentos.size() - 1);
                voo.setClasseCabine(getString(primeiro, "travel_class"));

                Object dur = oferta.get("duration");
                if (dur instanceof Map<?, ?> durMap) {
                    Object raw = durMap.get("raw");
                    if (raw instanceof Number n) voo.setDuracaoMinutos(n.intValue());
                }

                voo.setPreco(((Number) precoObj).doubleValue());
                voo.setMoeda("EUR");

                Object logoObj = oferta.get("airline_logo");
                if (logoObj != null) voo.setLogoUrl(logoObj.toString());

                if (guardarToken) {
                    Object tokenObj = oferta.get("next_token");
                    if (tokenObj != null) voo.setNextToken(tokenObj.toString());
                }

                voos.add(voo);
            }

        } catch (Exception e) {
            log.error("Erro ao processar ofertas de voos ({})", sentido, e);
        }

        return voos;
    }

    private String extrairHora(String datetime) {
        if (datetime == null || datetime.isBlank()) return "—";
        String[] partes = datetime.split("[ T]");
        if (partes.length >= 2) {
            String hora = partes[1];
            return hora.length() >= 5 ? hora.substring(0, 5) : hora;
        }
        return "—";
    }

    private String getString(Map<?, ?> map, String key) {
        if (map == null) return "";
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private String coalesce(String a, String b) {
        return (a != null && !a.isEmpty()) ? a : b;
    }
}
