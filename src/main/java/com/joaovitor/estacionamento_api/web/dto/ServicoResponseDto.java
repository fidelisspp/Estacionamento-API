package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(collectionRelation = "servicos", itemRelation = "servico")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ServicoResponseDto {

    @Schema(description = "Id do serviço", example = "1")
    private Long id;

    @Schema(description = "Nome do serviço", example = "Lavagem completa")
    private String nome;

    @Schema(description = "Descrição do serviço", example = "Lavagem externa e interna")
    private String descricao;

    @Schema(description = "Preço do serviço", example = "60.00")
    private BigDecimal preco;

    @Schema(description = "Tipo do serviço", example = "LAVAGEM")
    private String tipo;
}
