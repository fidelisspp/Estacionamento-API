package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.Servico;
import com.joaovitor.estacionamento_api.repository.projection.ServicoProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query("SELECT s FROM Servico s")
    Page<ServicoProjection> findAllPageable(Pageable pageable);

    Page<ServicoProjection> findAllByTipo(Servico.TipoServico tipo, Pageable pageable);

    @Query("SELECT COUNT(cv) > 0 FROM ClienteVaga cv JOIN cv.servicos s WHERE s.id = :id")
    boolean isUsadoEmEstacionamento(@Param("id") Long id);
}
