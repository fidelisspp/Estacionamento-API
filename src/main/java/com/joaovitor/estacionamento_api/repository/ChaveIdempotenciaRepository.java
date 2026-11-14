package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.ChaveIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChaveIdempotenciaRepository extends JpaRepository<ChaveIdempotencia, Long> {

    Optional<ChaveIdempotencia> findByApiKeyIdAndChave(Long apiKeyId, String chave);
}
