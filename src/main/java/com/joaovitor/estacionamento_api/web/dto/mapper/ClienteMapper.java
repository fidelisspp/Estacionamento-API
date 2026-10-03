package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.Cliente;
import com.joaovitor.estacionamento_api.repository.projection.ClienteProjection;
import com.joaovitor.estacionamento_api.web.dto.ClienteCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ClienteResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClienteMapper {

    public static Cliente toCliente(ClienteCreateDto dto) {
        return new ModelMapper().map(dto, Cliente.class);
    }

    public static ClienteResponseDto toDto(Cliente cliente) {
        return new ModelMapper().map(cliente, ClienteResponseDto.class);
    }

    public static ClienteResponseDto toDto(ClienteProjection projection) {
        return new ClienteResponseDto(projection.getId(), projection.getNome(), projection.getCpf());
    }
}
