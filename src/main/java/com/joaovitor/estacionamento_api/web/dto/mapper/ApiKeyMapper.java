package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.web.dto.ApiKeyResponseDto;
import com.joaovitor.estacionamento_api.web.dto.PlanoResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiKeyMapper {

    public static ApiKeyResponseDto toDto(ApiKey apiKey) {
        return new ModelMapper().map(apiKey, ApiKeyResponseDto.class);
    }

    public static PlanoResponseDto toDto(Plano plano) {
        return new PlanoResponseDto(plano.name(), plano.getLimitePorMinuto(),
                plano.getPrecoMensal(), plano.getDescricao());
    }
}
