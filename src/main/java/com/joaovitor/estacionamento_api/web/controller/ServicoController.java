package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Servico;
import com.joaovitor.estacionamento_api.service.ServicoService;
import com.joaovitor.estacionamento_api.web.dto.ServicoCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ServicoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ServicoMapper;
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

@Tag(name = "Servicos", description = "Contém todas as operações relativas aos recursos para cadastro, edição, leitura e exclusão dos serviços extras (lavagem, manobrista...).")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/servicos")
public class ServicoController {

    private final ServicoService servicoService;

    @Operation(summary = "Criar um novo serviço", description = "Recurso para criar um novo serviço extra. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL do recurso criado")),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Serviço com esse nome já cadastrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ServicoResponseDto>> create(@RequestBody @Valid ServicoCreateDto dto) {
        Servico servico = servicoService.salvar(ServicoMapper.toServico(dto));
        EntityModel<ServicoResponseDto> model = toModel(ServicoMapper.toDto(servico));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    private EntityModel<ServicoResponseDto> toModel(ServicoResponseDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ServicoController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ServicoController.class).update(dto.getId(), null)).withRel("update"),
                linkTo(methodOn(ServicoController.class).delete(dto.getId())).withRel("delete"),
                linkTo(methodOn(ServicoController.class).getAll(null, null)).withRel("servicos"));
    }

    @Operation(summary = "Localizar um serviço", description = "Recurso para retornar um serviço pelo id. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do serviço", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Serviço não localizado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<EntityModel<ServicoResponseDto>> getById(@PathVariable Long id) {
        Servico servico = servicoService.buscarPorId(id);
        return ResponseEntity.ok(toModel(ServicoMapper.toDto(servico)));
    }

    @Operation(summary = "Listar todos os serviços", description = "Recurso para listar todos os serviços de forma paginada. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
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
                    @ApiResponse(responseCode = "200", description = "Página com os serviços cadastrados (_embedded.servicos) e links de navegação")
            })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<PagedModel<EntityModel<ServicoResponseDto>>> getAll(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ServicoResponseDto> pagedAssembler) {
        Page<ServicoResponseDto> servicos = servicoService.buscarTodos(pageable).map(ServicoMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(servicos, this::toModel));
    }

    @Operation(summary = "Listar serviços por tipo", description = "Recurso para listar de forma paginada os serviços " +
            "do tipo informado. Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "tipo", description = "Tipo do serviço: LAVAGEM, MANOBRISTA, CALIBRAGEM ou OUTRO", required = true),
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
                    @ApiResponse(responseCode = "200", description = "Página com os serviços do tipo informado (_embedded.servicos) e links de navegação"),
                    @ApiResponse(responseCode = "400", description = "Tipo inválido",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<PagedModel<EntityModel<ServicoResponseDto>>> getAllByTipo(
            @PathVariable Servico.TipoServico tipo,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ServicoResponseDto> pagedAssembler) {
        Page<ServicoResponseDto> servicos = servicoService.buscarPorTipo(tipo, pageable).map(ServicoMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(servicos, this::toModel));
    }

    @Operation(summary = "Atualizar um serviço", description = "Recurso para atualizar todos os dados de um serviço. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do serviço", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Serviço não localizado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Serviço com esse nome já cadastrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ServicoResponseDto>> update(@PathVariable Long id,
                                                                  @RequestBody @Valid ServicoCreateDto dto) {
        Servico servico = servicoService.atualizar(id, ServicoMapper.toServico(dto));
        return ResponseEntity.ok(toModel(ServicoMapper.toDto(servico)));
    }

    @Operation(summary = "Excluir um serviço", description = "Recurso para excluir um serviço que nunca foi usado em um estacionamento. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do serviço", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Serviço excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Serviço não localizado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Serviço já usado em um estacionamento",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        servicoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
