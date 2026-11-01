package com.joaovitor.estacionamento_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocOpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("security", securityScheme())
                        .addSecuritySchemes("apiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("Chave de API do aplicativo. Obrigatória em todas as rotas /api/**")))
                .info(
                        new Info()
                                .title("REST API - Spring Park")
                                .description("API para gestão de estacionamento de veículos")
                                .version("v1")
                                .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                                .contact(new Contact().name("João Vitor").email("joaovitor@spring-park.com"))
                );
    }

    /**
     * Acrescenta a exigência da API Key em todas as operações de /api/**,
     * espelhando a regra do filtro num lugar só.
     */
    @Bean
    public OpenApiCustomizer apiKeyEmTodasAsRotas() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((caminho, item) -> {
                if (!caminho.startsWith("/api/")) {
                    return;
                }
                item.readOperations().forEach(operacao -> {
                    if (operacao.getSecurity() == null || operacao.getSecurity().isEmpty()) {
                        operacao.addSecurityItem(new SecurityRequirement().addList("apiKey"));
                    } else {
                        operacao.getSecurity().forEach(requisito -> requisito.addList("apiKey"));
                    }
                    operacao.getResponses().computeIfAbsent("401", codigo -> new ApiResponse()
                            .description("Chave de API ausente, inválida ou inativa, ou Bearer Token ausente/inválido")
                            .content(new Content().addMediaType("application/json",
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorMessage")))));
                });
            });
        };
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .description("Insira um bearer token valido para prosseguir")
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("security");
    }
}
