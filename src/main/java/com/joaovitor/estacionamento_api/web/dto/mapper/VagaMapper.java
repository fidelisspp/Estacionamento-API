package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.Vaga;
import com.joaovitor.estacionamento_api.repository.projection.VagaProjection;
import com.joaovitor.estacionamento_api.web.dto.VagaCreateDto;
import com.joaovitor.estacionamento_api.web.dto.VagaResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VagaMapper {

    public static Vaga toVaga(VagaCreateDto dto) {
        return new ModelMapper().map(dto, Vaga.class);
    }

    public static VagaResponseDto toDto(Vaga vaga) {
        return new ModelMapper().map(vaga, VagaResponseDto.class);
    }

    public static VagaResponseDto toDto(VagaProjection projection) {
        return new VagaResponseDto(projection.getId(), projection.getCodigo(), projection.getStatus());
    }
}
