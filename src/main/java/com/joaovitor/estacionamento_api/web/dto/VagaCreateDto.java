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
public class VagaCreateDto {

    @Schema(description = "Código da vaga com exatamente 4 caracteres", example = "A-01")
    @NotBlank
    @Size(min = 4, max = 4)
    private String codigo;

    @Schema(description = "Status da vaga", example = "LIVRE", allowableValues = {"LIVRE", "OCUPADA"})
    @NotBlank
    @Pattern(regexp = "LIVRE|OCUPADA")
    private String status;
}
