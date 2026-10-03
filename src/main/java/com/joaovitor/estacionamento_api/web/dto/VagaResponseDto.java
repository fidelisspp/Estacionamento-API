package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "vagas", itemRelation = "vaga")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VagaResponseDto {

    @Schema(description = "Id da vaga", example = "1")
    private Long id;

    @Schema(description = "Código da vaga", example = "A-01")
    private String codigo;

    @Schema(description = "Status da vaga: LIVRE ou OCUPADA", example = "LIVRE")
    private String status;
}
