package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.ChaveIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ChaveIdempotenciaRepository extends JpaRepository<ChaveIdempotencia, Long> {

    Optional<ChaveIdempotencia> findByApiKeyIdAndChave(Long apiKeyId, String chave);

    @Modifying
    @Query("delete from ChaveIdempotencia c where c.dataCriacao < :limite")
    int apagarCriadasAntesDe(@Param("limite") LocalDateTime limite);
}
