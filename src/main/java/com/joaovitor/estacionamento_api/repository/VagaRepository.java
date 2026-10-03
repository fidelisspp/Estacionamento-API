package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.Vaga;
import com.joaovitor.estacionamento_api.repository.projection.VagaProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    Optional<Vaga> findByCodigo(String codigo);

    Optional<Vaga> findFirstByStatus(Vaga.StatusVaga statusVaga);

    @Query("SELECT v FROM Vaga v")
    Page<VagaProjection> findAllPageable(Pageable pageable);

    Page<VagaProjection> findAllByStatus(Vaga.StatusVaga status, Pageable pageable);

    @Query("SELECT COUNT(cv) > 0 FROM ClienteVaga cv WHERE cv.vaga.id = :id AND cv.dataSaida IS NULL")
    boolean isComEstacionamentoEmAberto(@Param("id") Long id);

    @Query("SELECT COUNT(cv) > 0 FROM ClienteVaga cv WHERE cv.vaga.id = :id")
    boolean isUsadaEmEstacionamento(@Param("id") Long id);
}
