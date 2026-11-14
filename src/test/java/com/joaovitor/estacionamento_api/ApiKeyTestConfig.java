package com.joaovitor.estacionamento_api;

import com.joaovitor.estacionamento_api.apikey.ApiKeyFilter;
import com.joaovitor.estacionamento_api.apikey.RateLimitService;
import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.idempotencia.IdempotenciaFilter;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.RestTestClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

import java.util.UUID;

/**
 * Ajustes para os testes de integração convivirem com os filtros da API:
 * - envia a chave de API inicial (api.apikey.inicial) em toda requisição, pois /api/** exige X-API-Key;
 * - gera um X-Idempotency-Key novo em cada POST que não informar um;
 * - desliga o rate limit, já que a suíte inteira usa a mesma chave e passaria do limite do plano.
 */
@Configuration
public class ApiKeyTestConfig {

    @Bean
    public RestTestClientBuilderCustomizer apiKeyHeaderCustomizer(@Value("${api.apikey.inicial}") String chave) {
        return builder -> builder
                .defaultHeader(ApiKeyFilter.HEADER_API_KEY, chave)
                .requestInterceptor((request, body, execution) -> {
                    if (HttpMethod.POST.equals(request.getMethod())
                            && !request.getHeaders().containsHeader(IdempotenciaFilter.HEADER_IDEMPOTENCIA)) {
                        request.getHeaders().add(IdempotenciaFilter.HEADER_IDEMPOTENCIA, UUID.randomUUID().toString());
                    }
                    return execution.execute(request, body);
                });
    }

    @Bean
    @Primary
    public RateLimitService rateLimitServiceSemLimite() {
        return new RateLimitService() {
            @Override
            public ConsumptionProbe consumir(ApiKey apiKey) {
                return ConsumptionProbe.consumed(apiKey.getPlano().getLimitePorMinuto(), 0);
            }
        };
    }
}
