package com.joaovitor.estacionamento_api.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "estacionamentos", itemRelation = "estacionamento")
public class EstacionamentoResponseDto {

    @Schema(description = "Placa do veículo", example = "ABC-1234")
    private String placa;

    @Schema(description = "Marca do veículo", example = "FIAT")
    private String marca;

    @Schema(description = "Modelo do veículo", example = "PALIO 1.0")
    private String modelo;

    @Schema(description = "Cor do veículo", example = "AZUL")
    private String cor;

    @Schema(description = "CPF do cliente", example = "79074426050")
    private String clienteCpf;

    @Schema(description = "Número do recibo gerado no check-in", example = "20261003-143512")
    private String recibo;

    @Schema(description = "Data e hora de entrada", example = "2026-10-03 14:35:12", type = "string")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataEntrada;

    @Schema(description = "Data e hora de saída (preenchida só após o check-out)", example = "2026-10-03 16:05:40", type = "string")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataSaida;

    @Schema(description = "Código da vaga ocupada", example = "A-01")
    private String vagaCodigo;

    @Schema(description = "Valor cobrado no check-out (tempo + serviços extras)", example = "76.25")
    private BigDecimal valor;

    @Schema(description = "Desconto aplicado no check-out", example = "0.00")
    private BigDecimal desconto;

    @Schema(description = "Serviços extras contratados")
    private List<ServicoResponseDto> servicos;
}
