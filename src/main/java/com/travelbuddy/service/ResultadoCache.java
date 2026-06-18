package com.travelbuddy.service;

import com.travelbuddy.dto.TripResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ResultadoCache {

    private static final long TTL_MS = 10 * 60 * 1000; // 10 minutos

    public record Entrada(CompletableFuture<TripResponseDTO> future, String cidade, Instant criadoEm) {
        public Entrada(CompletableFuture<TripResponseDTO> future, String cidade) {
            this(future, cidade, Instant.now());
        }
        public boolean expirada() {
            return Instant.now().toEpochMilli() - criadoEm.toEpochMilli() > TTL_MS;
        }
    }

    private final ConcurrentHashMap<String, Entrada> cache = new ConcurrentHashMap<>();

    public void guardar(String id, Entrada entrada) {
        cache.put(id, entrada);
    }

    public Entrada obter(String id) {
        return cache.get(id);
    }

    public void remover(String id) {
        cache.remove(id);
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void limparExpiradas() {
        int antes = cache.size();
        cache.entrySet().removeIf(e -> e.getValue().expirada());
        int removidas = antes - cache.size();
        if (removidas > 0) log.info("ResultadoCache: {} entradas expiradas removidas", removidas);
    }
}
