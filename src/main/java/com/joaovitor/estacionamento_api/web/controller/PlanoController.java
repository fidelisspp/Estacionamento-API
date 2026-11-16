package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.web.dto.PlanoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.PlanoV2ResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ApiKeyMapper;
import com.joaovitor.estacionamento_api.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Planos", description = "Tabela de preços da API: planos disponíveis, com limite de requisições e preço mensal.")
@RestController
@RequestMapping("/api/v1/planos")
public class PlanoController {

    @Operation(summary = "Listar os planos (versão 1)", description = "Recurso para listar os planos da API, " +
            "versionado pelo header X-API-Version. " +
            "Versão 1 (padrão, quando o header não é enviado): limite por minuto (limitePorMinuto), preço mensal " +
            "(precoMensal) e descrição. " +
            "Versão 2 (X-API-Version: 2): agrupa os limites (limites.porMinuto e limites.porHora) e o preço " +
            "(preco.valor, preco.moeda e preco.formatado, ex.: R$ 60,00). " +
            "Versão não suportada responde 400. Não exige Bearer Token, apenas o header X-API-Key.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista dos planos (_embedded.planos), no formato da versão pedida"),
                    @ApiResponse(responseCode = "400", description = "Versão da API não suportada (versões disponíveis: 1, 2)",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping(version = "1")
    public ResponseEntity<CollectionModel<PlanoResponseDto>> getAll() {
        List<PlanoResponseDto> planos = Arrays.stream(Plano.values())
                .map(ApiKeyMapper::toDto)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(planos,
                linkTo(methodOn(PlanoController.class).getAll()).withSelfRel()));
    }

    @Operation(summary = "Listar os planos (versão 2)", description = "Versão 2 da tabela de preços: agrupa os limites " +
            "(por minuto e por hora) e o preço (valor, moeda e valor formatado). Envie o header X-API-Version: 2. " +
            "Não exige Bearer Token, apenas o header X-API-Key.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista dos planos no formato da versão 2 (_embedded.planos)")
            })
    @GetMapping(version = "2")
    public ResponseEntity<CollectionModel<PlanoV2ResponseDto>> getAllV2() {
        List<PlanoV2ResponseDto> planos = Arrays.stream(Plano.values())
                .map(ApiKeyMapper::toDtoV2)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(planos,
                linkTo(methodOn(PlanoController.class).getAllV2()).withSelfRel()));
    }
}
