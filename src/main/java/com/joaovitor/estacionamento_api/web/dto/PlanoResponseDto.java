package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "planos", itemRelation = "plano")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PlanoResponseDto {

    @Schema(description = "Nome do plano", example = "BASIC")
    private String nome;

    @Schema(description = "Requisições permitidas por minuto", example = "60")
    private int limitePorMinuto;

    @Schema(description = "Preço mensal do plano", example = "60.00")
    private BigDecimal precoMensal;

    @Schema(description = "Público-alvo do plano", example = "Estacionamento pequeno, com uma unidade")
    private String descricao;
}
