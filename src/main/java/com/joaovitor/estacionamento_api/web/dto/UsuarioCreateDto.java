package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class UsuarioCreateDto {

    @Schema(description = "E-mail do usuário, usado como login", example = "maria@email.com")
    @NotBlank
    @Email(message = "O formato de e-mail está invalido", regexp = "^[a-z0-9.+-]+@[a-z0-9.-]+\\.[a-z]{2,}$")
    private String username;

    @Schema(description = "Senha com exatamente 6 caracteres", example = "123456")
    @NotBlank
    @Size(min = 6, max = 6)
    private String password;
}
