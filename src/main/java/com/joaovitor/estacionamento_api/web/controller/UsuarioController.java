package com.joaovitor.estacionamento_api.web.controller;

import com.joaovitor.estacionamento_api.entity.Usuario;
import com.joaovitor.estacionamento_api.jwt.JwtUserDetails;
import com.joaovitor.estacionamento_api.service.UsuarioService;
import com.joaovitor.estacionamento_api.web.dto.UsuarioCreateDto;
import com.joaovitor.estacionamento_api.web.dto.UsuarioResponseDto;
import com.joaovitor.estacionamento_api.web.dto.UsuarioSenhaDto;
import com.joaovitor.estacionamento_api.web.dto.mapper.UsuarioMapper;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Usuarios", description = "Contém todas as operações relativas aos recursos para cadastro, edição e leitura de um usuário.")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    private JwtUserDetails usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof JwtUserDetails user) ? user : null;
    }

    private EntityModel<UsuarioResponseDto> toModel(UsuarioResponseDto dto) {
        EntityModel<UsuarioResponseDto> model = EntityModel.of(dto,
                linkTo(methodOn(UsuarioController.class).getById(dto.getId())).withSelfRel());

        JwtUserDetails logado = usuarioLogado();
        boolean ehOProprio = logado != null && logado.getId().equals(dto.getId());
        boolean ehAdmin = logado != null && logado.getRole().equals("ROLE_ADMIN");

        if (ehOProprio) {
            model.add(linkTo(methodOn(UsuarioController.class).updatePassword(dto.getId(), null)).withRel("update-password"));
        }
        if (ehAdmin) {
            if (!ehOProprio) {
                model.add(linkTo(methodOn(UsuarioController.class).delete(dto.getId(), null)).withRel("delete"));
            }
            model.add(linkTo(methodOn(UsuarioController.class).getAll(null, null)).withRel("usuarios"));
        }
        return model;
    }

    @Operation(summary = "Criar um novo usuário", description = "Recurso para criar um novo usuário",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Recurso criado com sucesso",
                            headers = @Header(name = HttpHeaders.LOCATION, description = "URL do recurso criado")),
                    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado no sistema",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Recurso não processado por dados de entrada invalidos",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PostMapping
    public ResponseEntity<EntityModel<UsuarioResponseDto>> create(@Valid @RequestBody UsuarioCreateDto createDto) {
        Usuario user = usuarioService.salvar(UsuarioMapper.toUsuario(createDto));
        EntityModel<UsuarioResponseDto> model = toModel(UsuarioMapper.toDto(user));
        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @Operation(summary = "Recuperar um usuário pelo id", description = "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recurso recuperado com sucesso, com os links HATEOAS permitidos a quem consulta"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('CLIENTE') AND #id == authentication.principal.id")
    public ResponseEntity<EntityModel<UsuarioResponseDto>> getById(@PathVariable Long id) {
        Usuario user = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(toModel(UsuarioMapper.toDto(user)));
    }

    @Operation(summary = "Atualizar senha", description = "Requisição exige um Bearer Token. Acesso restrito a ADMIN|CLIENTE",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))),
                    @ApiResponse(responseCode = "400", description = "Senha não confere",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "422", description = "Campos invalidos ou mal formatados",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE') AND (#id == authentication.principal.id)")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody UsuarioSenhaDto dto) {
        usuarioService.editarSenha(id, dto.getSenhaAtual(), dto.getNovaSenha(), dto.getConfirmaSenha());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar todos os usuários cadastrados", description = "Recurso para listar todos os usuários de forma paginada. " +
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
                    @ApiResponse(responseCode = "200", description = "Página com os usuários cadastrados (_embedded.usuarios) e links de navegação"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<UsuarioResponseDto>>> getAll(
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<UsuarioResponseDto> pagedAssembler) {
        Page<UsuarioResponseDto> users = usuarioService.buscarTodos(pageable).map(UsuarioMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(users, this::toModel));
    }

    @Operation(summary = "Listar usuários por perfil", description = "Recurso para listar de forma paginada os usuários " +
            "do perfil informado. Requisição exige um Bearer Token. Acesso restrito a ADMIN",
            security = @SecurityRequirement(name = "security"),
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "role", description = "Perfil do usuário: ADMIN ou CLIENTE", required = true),
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
                    @ApiResponse(responseCode = "200", description = "Página com os usuários do perfil informado (_embedded.usuarios) e links de navegação"),
                    @ApiResponse(responseCode = "400", description = "Perfil inválido",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<EntityModel<UsuarioResponseDto>>> getAllByRole(
            @PathVariable String role,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) PagedResourcesAssembler<UsuarioResponseDto> pagedAssembler) {
        Page<UsuarioResponseDto> users = usuarioService.buscarPorRole(role, pageable).map(UsuarioMapper::toDto);
        return ResponseEntity.ok(pagedAssembler.toModel(users, this::toModel));
    }

    @Operation(summary = "Excluir um usuário", description = "Requisição exige um Bearer Token. Acesso restrito a ADMIN. " +
            "Não é possível excluir o próprio usuário nem um usuário que tenha cadastro de cliente",
            security = @SecurityRequirement(name = "security"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Usuário sem permissão para acessar esse recurso",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
                    @ApiResponse(responseCode = "409", description = "Usuário é o próprio ADMIN logado ou possui cadastro de cliente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
            })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @Parameter(hidden = true) @AuthenticationPrincipal JwtUserDetails userDetails) {
        usuarioService.excluir(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
