package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.Servico;
import com.joaovitor.estacionamento_api.repository.projection.ServicoProjection;
import com.joaovitor.estacionamento_api.web.dto.ServicoCreateDto;
import com.joaovitor.estacionamento_api.web.dto.ServicoResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServicoMapper {

    public static Servico toServico(ServicoCreateDto dto) {
        return new ModelMapper().map(dto, Servico.class);
    }

    public static ServicoResponseDto toDto(Servico servico) {
        return new ModelMapper().map(servico, ServicoResponseDto.class);
    }

    public static ServicoResponseDto toDto(ServicoProjection projection) {
        return new ServicoResponseDto(projection.getId(), projection.getNome(), projection.getDescricao(),
                projection.getPreco(), projection.getTipo());
    }
}
