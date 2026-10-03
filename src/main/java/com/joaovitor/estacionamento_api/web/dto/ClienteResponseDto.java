package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "clientes", itemRelation = "cliente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ClienteResponseDto {

    @Schema(description = "Id do cliente", example = "1")
    private Long id;

    @Schema(description = "Nome completo do cliente", example = "Maria da Silva")
    private String nome;

    @Schema(description = "CPF do cliente, só números", example = "79074426050")
    private String cpf;
}
