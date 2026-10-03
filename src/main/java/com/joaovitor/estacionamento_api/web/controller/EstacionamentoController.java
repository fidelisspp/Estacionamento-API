package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.ClienteVaga;
import com.joaovitor.estacionamento_api.jwt.JwtUserDetails;
import com.joaovitor.estacionamento_api.service.ClienteVagaService;
import com.joaovitor.estacionamento_api.service.EstacionamentoService;
import com.joaovitor.estacionamento_api.web.dto.EstacionamentoCreateDto;
import com.joaovitor.estacionamento_api.web.dto.EstacionamentoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.ClienteVagaMapper;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Estacionamentos", description = "Contém todas as operações relativas ao registro de entrada (check-in) e saída (check-out) de um veículo do estacionamento.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/estacionamentos")
public class EstacionamentoController {

    private final EstacionamentoService estacionamentoService;
    private final ClienteVagaService clienteVagaService;

    private boolean isAdmin() {
        JwtUserDetails user = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return user.getRole().equals("ROLE_ADMIN");
    }

    private EntityModel<EstacionamentoResponseDto> toModel(EstacionamentoResponseDto dto) {
        EntityModel<EstacionamentoResponseDto> model = EntityModel.of(dto,
                linkTo(methodOn(EstacionamentoController.class).getByReciboEmQualquerEstado(dto.getRecibo())).withSelfRel());
        boolean emAberto = dto.getDataSaida() == null;

        if (isAdmin()) {
            if (emAberto) {
                model.add(linkTo(methodOn(EstacionamentoController.class).checkout(dto.getRecibo())).withRel("check-out"));
            } else {
                model.add(linkTo(methodOn(EstacionamentoController.class).delete(dto.getRecibo())).withRel("delete"));
            }
            model.add(linkTo(methodOn(ClienteController.class).getByCpf(dto.getClienteCpf())).withRel("cliente"));
            model.add(linkTo(methodOn(VagaController.class).getByCodigo(dto.getVagaCodigo())).withRel("vaga"));
            model.add(linkTo(methodOn(EstacionamentoController.class)
                    .getAllEstacionamentosPorCpf(dto.getClienteCpf(), null, null)).withRel("estacionamentos"));
        } else {
            model.add(linkTo(methodOn(ClienteController.class).getDetalhes(null)).withRel("cliente"));
            model.add(linkTo(methodOn(EstacionamentoController.class)
                    .getAllEstacionamentosDoCliente(null, null, null)).withRel("estacionamentos"));
        }
        return model;
    }

    @Operation(summary = "Operação de check-in", description = "Recurso para dar entrada de um veículo no estacionamento, " +
            "com serviços extras opcionais (servicosIds). Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL de acesso ao recurso criado")),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Causas possíveis: <br/>" +
                            "- CPF do cliente não cadastrado no sistema; <br/>" +
                            "- Nenhuma vaga livre foi localizada; <br/>" +
                            "- Serviço extra informado não encontrado.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping("/check-in")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<EstacionamentoResponseDto>> checkin(@RequestBody @Valid EstacionamentoCreateDto dto) {
        ClienteVaga clienteVaga = ClienteVagaMapper.toClienteVaga(dto);
        estacionamentoService.checkIn(clienteVaga, dto.getServicosIds());
        EntityModel<EstacionamentoResponseDto> model = toModel(ClienteVagaMapper.toDto(clienteVaga));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @Operation(summary = "Localizar um estacionamento", description = "Recurso para retornar um estacionamento, em aberto " +
            "ou finalizado, pelo nº do recibo. O cliente só acessa os próprios recibos. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "recibo", description = "Número do recibo gerado pelo check-in", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "O recibo não pertence ao cliente logado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Número do recibo não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{recibo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<EntityModel<EstacionamentoResponseDto>> getByReciboEmQualquerEstado(@PathVariable String recibo) {
        ClienteVaga clienteVaga = clienteVagaService.buscarPorReciboEmQualquerEstado(recibo);
        return ResponseEntity.ok(toModel(ClienteVagaMapper.toDto(clienteVaga)));
    }

    @Operation(summary = "Localizar um veículo estacionado", description = "Recurso para retornar um veículo estacionado " +
            "(check-in em aberto) pelo nº do recibo. O cliente só acessa os próprios recibos. " +
            "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "recibo", description = "Número do recibo gerado pelo check-in", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso localizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "O recibo não pertence ao cliente logado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Número do recibo não encontrado ou o veículo já passou pelo check-out",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/check-in/{recibo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<EntityModel<EstacionamentoResponseDto>> getByRecibo(@PathVariable String recibo) {
        ClienteVaga clienteVaga = clienteVagaService.buscarPorRecibo(recibo);
        return ResponseEntity.ok(toModel(ClienteVagaMapper.toDto(clienteVaga)));
    }

    @Operation(summary = "Operação de check-out", description = "Recurso para dar saída de um veículo do estacionamento, " +
            "calculando o valor (tempo + serviços extras) e o desconto. Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "recibo", description = "Número do recibo gerado pelo check-in", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso atualizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Número do recibo inexistente ou o veículo já passou pelo check-out",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PutMapping("/check-out/{recibo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<EstacionamentoResponseDto>> checkout(@PathVariable String recibo) {
        ClienteVaga clienteVaga = estacionamentoService.checkOut(recibo);
        return ResponseEntity.ok(toModel(ClienteVagaMapper.toDto(clienteVaga)));
    }

    @Operation(summary = "Excluir um estacionamento", description = "Recurso para excluir um estacionamento já finalizado " +
            "(check-out realizado). Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "recibo", description = "Número do recibo gerado pelo check-in", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Estacionamento excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Número do recibo não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Estacionamento ainda em aberto. Faça o check-out antes de excluir",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{recibo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String recibo) {
        clienteVagaService.excluir(recibo);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Localizar os registros de estacionamentos do cliente por CPF", description = "Recurso para listar " +
            "de forma paginada os estacionamentos de um cliente pelo CPF. Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "cpf", description = "Nº do CPF referente ao cliente a ser consultado", required = true),
                    @Parameter(in = ParameterIn.QUERY, name = "page",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "0")),
                            description = "Representa a página retornada"),
                    @Parameter(in = ParameterIn.QUERY, name = "size",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "5")),
                            description = "Representa o total de elementos por página"),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", hidden = true,
                            content = @Content(schema = @Schema(type = "string", defaultValue = "dataEntrada,asc")),
                            description = "Campo padrão de ordenação 'dataEntrada,asc'")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página com os estacionamentos do cliente (_embedded.estacionamentos) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/cpf/{cpf}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<EstacionamentoResponseDto>>> getAllEstacionamentosPorCpf(
            @PathVariable String cpf,
            @Parameter(hidden = true) @PageableDefault(size = 5, sort = "dataEntrada", direction = Sort.Direction.ASC) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<EstacionamentoResponseDto> pagedAssembler) {
        Page<EstacionamentoResponseDto> estacionamentos =
                clienteVagaService.buscarTodosPorClienteCpf(cpf, pageable).map(ClienteVagaMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(estacionamentos, this::toModel));
    }

    @Operation(summary = "Localizar os registros de estacionamentos do cliente logado", description = "Recurso para o cliente " +
            "listar de forma paginada os próprios estacionamentos, identificado pelo token. " +
            "Requisição exige um Bearer Token. Acesso restrito a CLIENTE",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "0")),
                            description = "Representa a página retornada"),
                    @Parameter(in = ParameterIn.QUERY, name = "size",
                            content = @Content(schema = @Schema(type = "integer", defaultValue = "5")),
                            description = "Representa o total de elementos por página"),
                    @Parameter(in = ParameterIn.QUERY, name = "sort", hidden = true,
                            content = @Content(schema = @Schema(type = "string", defaultValue = "dataEntrada,asc")),
                            description = "Campo padrão de ordenação 'dataEntrada,asc'")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Página com os estacionamentos do cliente logado (_embedded.estacionamentos) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<PagedModel<EntityModel<EstacionamentoResponseDto>>> getAllEstacionamentosDoCliente(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails user,
            @Parameter(hidden = true) @PageableDefault(size = 5, sort = "dataEntrada", direction = Sort.Direction.ASC) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<EstacionamentoResponseDto> pagedAssembler) {
        Page<EstacionamentoResponseDto> estacionamentos =
                clienteVagaService.buscarTodosPorUsuarioId(user.getId(), pageable).map(ClienteVagaMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(estacionamentos, this::toModel));
    }
}
