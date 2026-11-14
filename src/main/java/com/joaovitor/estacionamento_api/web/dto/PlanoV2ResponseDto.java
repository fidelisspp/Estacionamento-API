package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "planos", itemRelation = "plano")
public record PlanoV2ResponseDto(

        @Schema(description = "Nome do plano", example = "BASIC")
        String nome,

        @Schema(description = "Público-alvo do plano", example = "Estacionamento pequeno, com uma unidade")
        String descricao,

        @Schema(description = "Limites de requisições do plano")
        Limites limites,

        @Schema(description = "Preço mensal do plano")
        Preco preco) {

    public record Limites(
            @Schema(description = "Requisições por minuto", example = "60") int porMinuto,
            @Schema(description = "Requisições por hora", example = "3600") int porHora) {
    }

    public record Preco(
            @Schema(description = "Valor mensal", example = "60.00") BigDecimal valor,
            @Schema(description = "Moeda (ISO 4217)", example = "BRL") String moeda,
            @Schema(description = "Valor formatado para exibição", example = "R$ 60,00") String formatado) {
    }
}
