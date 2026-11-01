package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.service.ApiKeyService;
import com.joaovitor.estacionamento_api.web.dto.ApiKeyCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ApiKeyPlanoDto;
import com.joaovitor.estacionamento_api.web.dto.ApiKeyResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ApiKeyMapper;
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

@Tag(name = "API Keys", description = "Contém as operações para gerar, consultar, trocar o plano e desativar as chaves de API dos aplicativos.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @Operation(summary = "Gerar uma nova chave de API", description = "Recurso para gerar uma chave de API para um aplicativo. " +
            "O valor da chave é gerado pelo servidor (UUID). Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Chave criada com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL do recurso criado")),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ApiKeyResponseDto>> create(@RequestBody @Valid ApiKeyCreateDto dto) {
        ApiKey apiKey = apiKeyService.criar(dto.getNome(), Plano.valueOf(dto.getPlano()));
        EntityModel<ApiKeyResponseDto> model = toModel(ApiKeyMapper.toDto(apiKey));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    private EntityModel<ApiKeyResponseDto> toModel(ApiKeyResponseDto dto) {
        EntityModel<ApiKeyResponseDto> model = EntityModel.of(dto,
                linkTo(methodOn(ApiKeyController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ApiKeyController.class).trocarPlano(dto.getId(), null)).withRel("trocar-plano"),
                linkTo(methodOn(ApiKeyController.class).getAll(null, null)).withRel("api-keys"),
                linkTo(methodOn(PlanoController.class).getAll()).withRel("planos"));
        if (dto.isAtiva()) {
            model.add(linkTo(methodOn(ApiKeyController.class).desativar(dto.getId())).withRel("desativar"));
        }
        return model;
    }

    @Operation(summary = "Localizar uma chave de API", description = "Recurso para retornar uma chave pelo id. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id da chave", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Chave não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ApiKeyResponseDto>> getById(@PathVariable Long id) {
        ApiKey apiKey = apiKeyService.buscarPorId(id);
        return ResponseEntity.ok(toModel(ApiKeyMapper.toDto(apiKey)));
    }

    @Operation(summary = "Listar todas as chaves de API", description = "Recurso para listar as chaves de forma paginada. " +
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
                    @ApiResponse(responseCode = "200", description = "Página com as chaves cadastradas (_embedded.apiKeys) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<ApiKeyResponseDto>>> getAll(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ApiKeyResponseDto> pagedAssembler) {
        Page<ApiKeyResponseDto> chaves = apiKeyService.buscarTodas(pageable).map(ApiKeyMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(chaves, this::toModel));
    }

    @Operation(summary = "Trocar o plano de uma chave", description = "Recurso para alterar o plano contratado de uma chave. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id da chave", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Plano alterado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Chave não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Plano inválido",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PatchMapping("/{id}/plano")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ApiKeyResponseDto>> trocarPlano(@PathVariable Long id,
                                                                     @RequestBody @Valid ApiKeyPlanoDto dto) {
        ApiKey apiKey = apiKeyService.trocarPlano(id, Plano.valueOf(dto.getPlano()));
        return ResponseEntity.ok(toModel(ApiKeyMapper.toDto(apiKey)));
    }

    @Operation(summary = "Desativar uma chave de API", description = "Recurso para desativar uma chave. A chave não é excluída: " +
            "continua no histórico, mas deixa de ser aceita. Não é possível desativar a última chave ativa. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id da chave", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Chave desativada com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Chave não localizada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Chave é a última ativa e não pode ser desativada",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        apiKeyService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
