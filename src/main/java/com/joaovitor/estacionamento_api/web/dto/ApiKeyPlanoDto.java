package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ApiKeyPlanoDto {

    @Schema(description = "Novo plano da chave", example = "PRO", allowableValues = {"FREE", "BASIC", "PRO"})
    @NotBlank
    @Pattern(regexp = "FREE|BASIC|PRO")
    private String plano;
}
