package com.travelbuddy.service;

import com.travelbuddy.dto.GuardarViagemDTO;
import com.travelbuddy.model.Viagem;
import com.travelbuddy.repository.ViagemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViagemService {

    private final ViagemRepository repository;

    public Viagem guardar(String emailUtilizador, GuardarViagemDTO dto) {
        Viagem v = new Viagem();
        v.setEmailUtilizador(emailUtilizador);
        v.setCidade(dto.getCidade());
        v.setDataInicio(dto.getDataInicio());
        v.setDataFim(dto.getDataFim());
        v.setLat(dto.getLat());
        v.setLon(dto.getLon());

        v.setHotelNome(dto.getHotelNome());
        v.setHotelMorada(dto.getHotelMorada());
        v.setHotelPreco(dto.getHotelPreco());
        v.setHotelRating(dto.getHotelRating());
        v.setHotelFoto(dto.getHotelFoto());

        v.setVooIdaOrigem(dto.getVooIdaOrigem());
        v.setVooIdaDestino(dto.getVooIdaDestino());
        v.setVooIdaCompanhia(dto.getVooIdaCompanhia());
        v.setVooIdaHoraPartida(dto.getVooIdaHoraPartida());
        v.setVooIdaHoraChegada(dto.getVooIdaHoraChegada());
        v.setVooIdaPreco(dto.getVooIdaPreco());
        v.setVooIdaLogoUrl(dto.getVooIdaLogoUrl());
        v.setVooIdaEscalas(dto.getVooIdaEscalas());
        v.setVooIdaDuracaoMinutos(dto.getVooIdaDuracaoMinutos());

        v.setMeteoTemperaturaMedia(dto.getMeteoTemperaturaMedia());
        v.setMeteoTemperaturaMin(dto.getMeteoTemperaturaMin());
        v.setMeteoTemperaturaMax(dto.getMeteoTemperaturaMax());
        v.setMeteoDescricao(dto.getMeteoDescricao());
        v.setMeteoProbabilidadeChuva(dto.getMeteoProbabilidadeChuva());
        v.setMeteoVentoKmh(dto.getMeteoVentoKmh());

        v.setVooRegressoOrigem(dto.getVooRegressoOrigem());
        v.setVooRegressoDestino(dto.getVooRegressoDestino());
        v.setVooRegressoCompanhia(dto.getVooRegressoCompanhia());
        v.setVooRegressoHoraPartida(dto.getVooRegressoHoraPartida());
        v.setVooRegressoHoraChegada(dto.getVooRegressoHoraChegada());
        v.setVooRegressoPreco(dto.getVooRegressoPreco());
        v.setVooRegressoLogoUrl(dto.getVooRegressoLogoUrl());
        v.setVooRegressoEscalas(dto.getVooRegressoEscalas());
        v.setVooRegressoDuracaoMinutos(dto.getVooRegressoDuracaoMinutos());

        Viagem guardada = repository.save(v);
        log.info("Viagem guardada: id={}, cidade={}", guardada.getId(), dto.getCidade());
        return guardada;
    }

    public List<Viagem> listar(String emailUtilizador) {
        return repository.findByEmailUtilizadorOrderByGuardadoEmDesc(emailUtilizador);
    }

    public void apagar(Long id, String emailUtilizador) {
        repository.findById(id).ifPresent(v -> {
            if (v.getEmailUtilizador().equals(emailUtilizador)) {
                repository.delete(v);
                log.info("Viagem apagada: id={}", id);
            }
        });
    }
}
