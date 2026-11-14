package com.joaovitor.estacionamento_api.web.dto.mapper;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.web.dto.ApiKeyResponseDto;
import com.joaovitor.estacionamento_api.web.dto.PlanoResponseDto;
import com.joaovitor.estacionamento_api.web.dto.PlanoV2ResponseDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;

import java.text.NumberFormat;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiKeyMapper {

    public static ApiKeyResponseDto toDto(ApiKey apiKey) {
        return new ModelMapper().map(apiKey, ApiKeyResponseDto.class);
    }

    public static PlanoResponseDto toDto(Plano plano) {
        return new PlanoResponseDto(plano.name(), plano.getLimitePorMinuto(),
                plano.getPrecoMensal(), plano.getDescricao());
    }

    public static PlanoV2ResponseDto toDtoV2(Plano plano) {
        NumberFormat moedaBrasil = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
        return new PlanoV2ResponseDto(
                plano.name(),
                plano.getDescricao(),
                new PlanoV2ResponseDto.Limites(plano.getLimitePorMinuto(), plano.getLimitePorMinuto() * 60),
                new PlanoV2ResponseDto.Preco(plano.getPrecoMensal(), "BRL", moedaBrasil.format(plano.getPrecoMensal())));
    }
}
