package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(collectionRelation = "apiKeys", itemRelation = "apiKey")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApiKeyResponseDto {

    @Schema(description = "Id da chave", example = "1")
    private Long id;

    @Schema(description = "Valor da chave, enviado no header X-API-Key", example = "92c8f74b-2a40-443d-9192-8c567366e445")
    private String chave;

    @Schema(description = "Nome do aplicativo", example = "App Estacionamento Centro")
    private String nome;

    @Schema(description = "Plano contratado", example = "BASIC")
    private String plano;

    @Schema(description = "Indica se a chave está ativa", example = "true")
    private boolean ativa;

    @Schema(description = "Data de criação da chave", example = "2026-10-31T20:00:00")
    private LocalDateTime dataCriacao;
}
