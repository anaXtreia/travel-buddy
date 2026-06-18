package com.travelbuddy.service;

import com.travelbuddy.dto.AeroportoDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class AeroportoService {

    private List<AeroportoDTO> aeroportos = List.of();

    @PostConstruct
    public void carregar() {
        List<AeroportoDTO> lista = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("airports.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String linha;
            boolean primeiraLinha = true;
            while ((linha = reader.readLine()) != null) {
                if (primeiraLinha) { primeiraLinha = false; continue; }

                String[] cols = parseCsvLinha(linha);
                if (cols.length < 14) continue;

                String type    = strip(cols[2]);
                String iata    = strip(cols[13]);
                String nome    = strip(cols[3]);
                String cidade  = strip(cols[10]);
                String pais    = strip(cols[8]);

                if (iata.isEmpty()) continue;
                if (!type.equals("large_airport") && !type.equals("medium_airport")) continue;

                AeroportoDTO dto = new AeroportoDTO();
                dto.setIata(iata);
                dto.setNome(nome);
                dto.setCidade(cidade.isEmpty() ? nome : cidade);
                dto.setPais(pais);
                lista.add(dto);
            }

            aeroportos = List.copyOf(lista);
            log.info("Aeroportos carregados: {}", aeroportos.size());

        } catch (Exception e) {
            log.error("Erro ao carregar airports.csv", e);
        }
    }

    public List<AeroportoDTO> pesquisar(String q) {
        if (q == null || q.isBlank()) return List.of();
        String termo = q.toLowerCase(Locale.ROOT);
        return aeroportos.stream()
                .filter(a -> a.getIata().toLowerCase().contains(termo)
                          || a.getNome().toLowerCase().contains(termo)
                          || a.getCidade().toLowerCase().contains(termo))
                .limit(8)
                .toList();
    }

    /** Remove aspas e espaços à volta do valor CSV. */
    private String strip(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1);
        return s.trim();
    }

    private String[] parseCsvLinha(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean dentroAspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroAspas = !dentroAspas;
            } else if (c == ',' && !dentroAspas) {
                campos.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        campos.add(sb.toString());
        return campos.toArray(new String[0]);
    }
}
