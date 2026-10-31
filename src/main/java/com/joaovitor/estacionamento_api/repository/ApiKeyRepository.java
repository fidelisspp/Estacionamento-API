package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByChave(String chave);

    long countByAtivaTrue();
}