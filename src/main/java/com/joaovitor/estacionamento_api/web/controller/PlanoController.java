package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.web.dto.PlanoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ApiKeyMapper;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Listar os planos", description = "Recurso para listar os planos da API com limite por minuto, " +
            "preço mensal e descrição. Não exige Bearer Token, apenas o header X-API-Key.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Lista dos planos (_embedded.planos)")
            })
    @GetMapping
    public ResponseEntity<CollectionModel<PlanoResponseDto>> getAll() {
        List<PlanoResponseDto> planos = Arrays.stream(Plano.values())
                .map(ApiKeyMapper::toDto)
                .toList();
        return ResponseEntity.ok(CollectionModel.of(planos,
                linkTo(methodOn(PlanoController.class).getAll()).withSelfRel()));
    }
}
