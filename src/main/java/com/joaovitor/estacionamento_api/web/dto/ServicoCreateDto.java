package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ServicoCreateDto {

    @Schema(description = "Nome do serviço, único (3 a 60 caracteres)", example = "Lavagem completa")
    @NotBlank
    @Size(min = 3, max = 60)
    private String nome;

    @Schema(description = "Descrição opcional do serviço (até 255 caracteres)", example = "Lavagem externa e interna")
    @Size(max = 255)
    private String descricao;

    @Schema(description = "Preço do serviço, maior que zero", example = "60.00")
    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    private BigDecimal preco;

    @Schema(description = "Tipo do serviço", example = "LAVAGEM", allowableValues = {"LAVAGEM", "MANOBRISTA", "CALIBRAGEM", "OUTRO"})
    @NotBlank
    @Pattern(regexp = "LAVAGEM|MANOBRISTA|CALIBRAGEM|OUTRO")
    private String tipo;
}
