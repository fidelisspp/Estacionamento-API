package com.joaovitor.estacionamento_api.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstacionamentoCreateDto {

    @Schema(description = "Placa do veículo no padrão XXX-0000", example = "ABC-1234")
    @NotBlank
    @Size(min = 8, max = 8)
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{4}", message = "A placa do veículo deve seguir o padrão \"XXX-0000\"")
    private String placa;

    @Schema(description = "Marca do veículo", example = "FIAT")
    @NotBlank
    private String marca;

    @Schema(description = "Modelo do veículo", example = "PALIO 1.0")
    @NotBlank
    private String modelo;

    @Schema(description = "Cor do veículo", example = "AZUL")
    @NotBlank
    private String cor;

    @Schema(description = "CPF do cliente cadastrado, só números", example = "79074426050")
    @NotBlank
    @Size(min = 11, max = 11)
    @CPF
    private String clienteCpf;

    @Schema(description = "Ids dos serviços extras contratados (opcional)", example = "[1, 2]")
    private List<@NotNull Long> servicosIds;
}
