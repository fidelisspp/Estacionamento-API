package com.joaovitor.estacionamento_api.config;

import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.repository.ApiKeyRepository;
import com.joaovitor.estacionamento_api.service.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ApiKeyInicialConfig {

    private static final int TAMANHO_MAXIMO_CHAVE = 36;

    @Bean
    public ApplicationRunner criarApiKeyInicial(ApiKeyRepository apiKeyRepository,
                                                ApiKeyService apiKeyService,
                                                @Value("${api.apikey.inicial:}") String chaveInicial) {
        return args -> {
            if (apiKeyRepository.count() > 0) {
                return;
            }

            String chave = chaveInicial == null ? "" : chaveInicial.trim();

            if (chave.isEmpty()) {
                throw new IllegalStateException(
                        "Nenhuma chave de API cadastrada. Defina a variável de ambiente API_KEY_INICIAL " +
                                "(sugestão: um UUID) para criar a chave inicial.");
            }
            if (chave.length() > TAMANHO_MAXIMO_CHAVE) {
                throw new IllegalStateException(String.format(
                        "API_KEY_INICIAL deve ter no máximo %d caracteres (recebido: %d).",
                        TAMANHO_MAXIMO_CHAVE, chave.length()));
            }

            apiKeyService.criarComChave("Chave inicial", Plano.PRO, chave);
            log.info("Chave de API inicial criada com o plano {}", Plano.PRO);
        };
    }
}