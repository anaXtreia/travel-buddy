package com.travelbuddy.repository;

import com.travelbuddy.model.Viagem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ViagemRepository extends JpaRepository<Viagem, Long> {
    List<Viagem> findByEmailUtilizadorOrderByGuardadoEmDesc(String emailUtilizador);
}
