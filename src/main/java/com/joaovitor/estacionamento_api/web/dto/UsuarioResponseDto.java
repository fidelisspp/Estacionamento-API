package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UsuarioResponseDto {

    @Schema(description = "Id do usuário", example = "1")
    private Long id;

    @Schema(description = "E-mail do usuário", example = "maria@email.com")
    private String username;

    @Schema(description = "Perfil do usuário: ADMIN ou CLIENTE", example = "CLIENTE")
    private String role;
}
