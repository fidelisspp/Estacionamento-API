package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Vaga;
import com.joaovitor.estacionamento_api.service.VagaService;
import com.joaovitor.estacionamento_api.web.dto.VagaCreateDto;
import com.joaovitor.estacionamento_api.web.dto.VagaResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.VagaMapper;
import com.joaovitor.estacionamento_api.web.exception.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Vagas", description = "Contém todas as operações relativas aos recursos para cadastro, edição, leitura e exclusão de uma vaga.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/vagas")
public class VagaController {

    private final VagaService vagaService;

    @Operation(summary = "Criar uma nova vaga", description = "Recurso para criar uma nova vaga. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL do recurso criado")),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Vaga já cadastrada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<VagaResponseDto>> create(@RequestBody @Valid VagaCreateDto dto) {
        Vaga vaga = vagaService.salvar(VagaMapper.toVaga(dto));
        EntityModel<VagaResponseDto> model = toModel(VagaMapper.toDto(vaga));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    private EntityModel<VagaResponseDto> toModel(VagaResponseDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(VagaController.class).getByCodigo(dto.getCodigo())).withSelfRel(),
                linkTo(methodOn(VagaController.class).update(dto.getCodigo(), null)).withRel("update"),
                linkTo(methodOn(VagaController.class).delete(dto.getCodigo())).withRel("delete"),
                linkTo(methodOn(VagaController.class).getAll(null, null)).withRel("vagas"));
    }

    @Operation(summary = "Localizar uma vaga", description = "Recurso para retornar uma vaga pelo seu código. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "codigo", description = "Código da vaga (ex.: A-01)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Vaga não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<VagaResponseDto>> getByCodigo(@PathVariable String codigo) {
        Vaga vaga = vagaService.buscarPorCodigo(codigo);
        return ResponseEntity.ok(toModel(VagaMapper.toDto(vaga)));
    }

    @Operation(summary = "Listar todas as vagas", description = "Recurso para listar todas as vagas de forma paginada. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "0")),
                            description = "Representa a página retornada"),
                    @Parameter(in = ParameterIn.QUERY, name = "size",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "20")),
                            description = "Representa o total de elementos por página"),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", hidden = true,
                            content = @Content(schema = @Schema(type = "string", defaultValue = "id,asc")),
                            description = "Representa a ordenação dos resultados. Aceita múltiplos critérios de ordenação")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página com as vagas cadastradas (_embedded.vagas) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<VagaResponseDto>>> getAll(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<VagaResponseDto> pagedAssembler) {
        Page<VagaResponseDto> vagas = vagaService.buscarTodos(pageable).map(VagaMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(vagas, this::toModel));
    }

    @Operation(summary = "Listar vagas por status", description = "Recurso para listar de forma paginada as vagas " +
            "com o status informado (LIVRE ou OCUPADA). Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "status", description = "Status da vaga: LIVRE ou OCUPADA", required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "page",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "0")),
                            description = "Representa a página retornada"),
                    @Parameter(in = ParameterIn.QUERY, name = "size",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "20")),
                            description = "Representa o total de elementos por página"),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", hidden = true,
                            content = @Content(schema = @Schema(type = "string", defaultValue = "id,asc")),
                            description = "Representa a ordenação dos resultados. Aceita múltiplos critérios de ordenação")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página com as vagas do status informado (_embedded.vagas) e links de navegação"),
                    @ApiResponse(responseCode = "400", description = "Status inválido",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<VagaResponseDto>>> getAllByStatus(
            @PathVariable Vaga.StatusVaga status,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<VagaResponseDto> pagedAssembler) {
        Page<VagaResponseDto> vagas = vagaService.buscarPorStatus(status, pageable).map(VagaMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(vagas, this::toModel));
    }

    @Operation(summary = "Atualizar uma vaga", description = "Recurso para atualizar o código e o status de uma vaga. " +
            "Não é possível alterar uma vaga com veículo estacionado. Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "codigo", description = "Código atual da vaga", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Vaga não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Novo código já cadastrado ou vaga com veículo estacionado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PutMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<VagaResponseDto>> update(@PathVariable String codigo,
                                                               @RequestBody @Valid VagaCreateDto dto) {
        Vaga vaga = vagaService.atualizar(codigo, VagaMapper.toVaga(dto));
        return ResponseEntity.ok(toModel(VagaMapper.toDto(vaga)));
    }

    @Operation(summary = "Excluir uma vaga", description = "Recurso para excluir uma vaga que nunca foi usada em um estacionamento. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "codigo", description = "Código da vaga", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Vaga excluída com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Vaga não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Vaga já usada em um estacionamento",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String codigo) {
        vagaService.excluir(codigo);
        return ResponseEntity.noContent().build();
    }
}
