package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class UsuarioSenhaDto {

    @Schema(description = "Senha atual do usuário", example = "123456")
    @NotBlank
    @Size(min = 6, max = 6)
    private String senhaAtual;

    @Schema(description = "Nova senha com exatamente 6 caracteres", example = "654321")
    @NotBlank
    @Size(min = 6, max = 6)
    private String novaSenha;

    @Schema(description = "Repetição da nova senha, deve ser igual a novaSenha", example = "654321")
    @NotBlank
    @Size(min = 6, max = 6)
    private String confirmaSenha;
}
