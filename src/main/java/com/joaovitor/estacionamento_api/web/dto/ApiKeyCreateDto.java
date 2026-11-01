package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApiKeyCreateDto {

    @Schema(description = "Nome do aplicativo que vai usar a chave (3 a 100 caracteres)", example = "App Estacionamento Centro")
    @NotBlank
    @Size(min = 3, max = 100)
    private String nome;

    @Schema(description = "Plano contratado", example = "BASIC", allowableValues = {"FREE", "BASIC", "PRO"})
    @NotBlank
    @Pattern(regexp = "FREE|BASIC|PRO")
    private String plano;
}
