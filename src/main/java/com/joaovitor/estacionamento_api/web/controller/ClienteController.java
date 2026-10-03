package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Cliente;
import com.joaovitor.estacionamento_api.jwt.JwtUserDetails;
import com.joaovitor.estacionamento_api.service.ClienteService;
import com.joaovitor.estacionamento_api.service.UsuarioService;
import com.joaovitor.estacionamento_api.web.dto.ClienteCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ClienteResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ClienteMapper;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Clientes", description = "Contém todas as operações relativas aos recursos para cadastro, edição, leitura e exclusão de um cliente.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    private final UsuarioService usuarioService;

    private EntityModel<ClienteResponseDto> toModel(ClienteResponseDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ClienteController.class).getById(dto.getId())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).update(dto.getId(), null)).withRel("update"),
                linkTo(methodOn(ClienteController.class).delete(dto.getId())).withRel("delete"),
                linkTo(methodOn(ClienteController.class).getAll(null, null)).withRel("clientes"),
                linkTo(methodOn(EstacionamentoController.class)
                        .getAllEstacionamentosPorCpf(dto.getCpf(), null, null)).withRel("estacionamentos"));
    }

    private EntityModel<ClienteResponseDto> toModelDoCliente(ClienteResponseDto dto) {
        return EntityModel.of(dto,
                linkTo(methodOn(ClienteController.class).getDetalhes(null)).withSelfRel(),
                linkTo(methodOn(EstacionamentoController.class)
                        .getAllEstacionamentosDoCliente(null, null, null)).withRel("estacionamentos"));
    }

    @Operation(summary = "Criar um novo cliente", description = "Recurso para criar um novo cliente vinculado ao usuário logado. " +
            "Requisição exige um Bearer Token. Acesso restrito a CLIENTE",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL de acesso aos dados do cliente logado (/detalhes)")),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "CPF já cadastrado no sistema",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<EntityModel<ClienteResponseDto>> create(@RequestBody @Valid ClienteCreateDto dto,
                                                                  @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        Cliente cliente = ClienteMapper.toCliente(dto);
        cliente.setUsuario(usuarioService.buscarPorId(userDetails.getId()));
        clienteService.salvar(cliente);

        EntityModel<ClienteResponseDto> model = toModelDoCliente(ClienteMapper.toDto(cliente));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @Operation(summary = "Localizar um cliente", description = "Recurso para localizar um cliente pelo id. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do cliente", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ClienteResponseDto>> getById(@PathVariable Long id) {
        Cliente cliente = clienteService.buscarPorId(id);
        return ResponseEntity.ok(toModel(ClienteMapper.toDto(cliente)));
    }

    @Operation(summary = "Recuperar lista de clientes", description = "Recurso para listar todos os clientes de forma paginada. " +
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
                    @ApiResponse(responseCode = "200", description = "Página com os clientes cadastrados (_embedded.clientes) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<ClienteResponseDto>>> getAll(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<ClienteResponseDto> pagedAssembler) {
        Page<ClienteResponseDto> clientes = clienteService.buscarTodos(pageable).map(ClienteMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(clientes, this::toModel));
    }

    @Operation(summary = "Recuperar dados do cliente autenticado", description = "Recurso para o cliente consultar os próprios dados, " +
            "identificado pelo token. Requisição exige um Bearer Token. Acesso restrito a CLIENTE",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Usuário logado ainda não possui cadastro de cliente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/detalhes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<EntityModel<ClienteResponseDto>> getDetalhes(@Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        Cliente cliente = clienteService.buscarPorUsuarioId(userDetails.getId());
        return ResponseEntity.ok(toModelDoCliente(ClienteMapper.toDto(cliente)));
    }

    @Operation(summary = "Localizar um cliente pelo CPF", description = "Recurso para localizar um cliente pelo número do CPF. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "cpf", description = "Nº do CPF do cliente, só números (11 dígitos)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "CPF não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ClienteResponseDto>> getByCpf(@PathVariable String cpf) {
        Cliente cliente = clienteService.buscarPorCpf(cpf);
        return ResponseEntity.ok(toModel(ClienteMapper.toDto(cliente)));
    }

    @Operation(summary = "Atualizar um cliente", description = "Recurso para atualizar o nome e o CPF de um cliente. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do cliente", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "CPF já cadastrado para outro cliente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<ClienteResponseDto>> update(@PathVariable Long id,
                                                                  @RequestBody @Valid ClienteCreateDto dto) {
        Cliente cliente = clienteService.atualizar(id, ClienteMapper.toCliente(dto));
        return ResponseEntity.ok(toModel(ClienteMapper.toDto(cliente)));
    }

    @Operation(summary = "Excluir um cliente", description = "Recurso para excluir um cliente sem histórico de estacionamentos. " +
            "O usuário vinculado não é excluído. Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "id", description = "Id do cliente", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Cliente excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Cliente não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Cliente possui histórico de estacionamentos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
