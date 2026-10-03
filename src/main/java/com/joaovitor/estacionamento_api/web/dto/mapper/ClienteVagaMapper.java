package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.ClienteVaga;
import com.joaovitor.estacionamento_api.repository.projection.ClienteVagaProjection;
import com.joaovitor.estacionamento_api.web.dto.EstacionamentoCreateDto;
import com.joaovitor.estacionamento_api.web.dto.EstacionamentoResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClienteVagaMapper {

    public static ClienteVaga toClienteVaga(EstacionamentoCreateDto dto) {
        return new ModelMapper().map(dto, ClienteVaga.class);
    }

    public static EstacionamentoResponseDto toDto(ClienteVaga clienteVaga) {
        return new ModelMapper().map(clienteVaga, EstacionamentoResponseDto.class);
    }

    public static EstacionamentoResponseDto toDto(ClienteVagaProjection projection) {
        EstacionamentoResponseDto dto = new EstacionamentoResponseDto();
        dto.setRecibo(projection.getRecibo());
        dto.setPlaca(projection.getPlaca());
        dto.setMarca(projection.getMarca());
        dto.setModelo(projection.getModelo());
        dto.setCor(projection.getCor());
        dto.setClienteCpf(projection.getClienteCpf());
        dto.setDataEntrada(projection.getDataEntrada());
        dto.setDataSaida(projection.getDataSaida());
        dto.setVagaCodigo(projection.getVagaCodigo());
        dto.setValor(projection.getValor());
        dto.setDesconto(projection.getDesconto());
        return dto;
    }
}
