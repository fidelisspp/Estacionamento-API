package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ClienteCreateDto {

    @Schema(description = "Nome completo do cliente (5 a 100 caracteres)", example = "Maria da Silva")
    @NotBlank
    @Size(min = 5, max = 100)
    private String nome;

    @Schema(description = "CPF válido, só números (11 dígitos)", example = "79074426050")
    @NotBlank
    @Size(min = 11, max = 11)
    @CPF
    private String cpf;
}
