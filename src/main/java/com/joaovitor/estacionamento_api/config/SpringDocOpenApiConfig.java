package com.joaovitor.estacionamento_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
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
                                .termsOfService("https://github.com/fidelisspp/Estacionamento-API/blob/main/TERMOS-DE-USO.md")
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
                    operacao.getResponses().computeIfAbsent("429", codigo -> new ApiResponse()
                            .description("Limite de requisições do plano excedido (FREE 10, BASIC 60, PRO 150 por minuto). " +
                                    "Aguarde o tempo informado no header Retry-After")
                            .addHeaderObject("Retry-After", new Header()
                                    .description("Segundos até a próxima requisição ser aceita")
                                    .schema(new IntegerSchema()))
                            .addHeaderObject("X-Rate-Limit-Limit", new Header()
                                    .description("Limite de requisições por minuto do plano da chave")
                                    .schema(new IntegerSchema()))
                            .addHeaderObject("X-Rate-Limit-Remaining", new Header()
                                    .description("Requisições restantes no momento (0 quando o limite é excedido)")
                                    .schema(new IntegerSchema()))
                            .content(new Content().addMediaType("application/json",
                                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorMessage")))));
                });
                if (!caminho.equals("/api/v1/auth") && item.getPost() != null) {
                    documentarIdempotencia(item.getPost());
                }
            });
        };
    }

    private void documentarIdempotencia(Operation operacao) {
        operacao.addParametersItem(new HeaderParameter()
                .name("X-Idempotency-Key")
                .required(true)
                .description("UUID único por operação. Reenviar a mesma chave devolve a resposta original, " +
                        "sem processar de novo. Validade: 24 horas")
                .schema(new StringSchema().format("uuid").example("9064f977-2733-493c-b60a-106ed1b6f5c8")));

        complementarResposta(operacao, "400", "Header X-Idempotency-Key ausente ou não é um UUID");
        complementarResposta(operacao, "409", "Requisição com a mesma X-Idempotency-Key ainda em processamento");
        complementarResposta(operacao, "422", "X-Idempotency-Key já usada com outro endpoint, corpo ou usuário");

        operacao.getResponses().forEach((codigo, resposta) -> {
            if (codigo.startsWith("2")) {
                resposta.addHeaderObject("X-Idempotency-Replayed", new Header()
                        .description("Vem com o valor true quando a resposta foi devolvida do armazenamento de " +
                                "idempotência, sem processar a requisição de novo")
                        .schema(new StringSchema()));
            }
        });
    }

    private void complementarResposta(Operation operacao, String codigo, String texto) {
        ApiResponse existente = operacao.getResponses().get(codigo);
        if (existente == null) {
            operacao.getResponses().addApiResponse(codigo, new ApiResponse()
                    .description(texto)
                    .content(new Content().addMediaType("application/json",
                            new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorMessage")))));
        } else {
            existente.setDescription(existente.getDescription() + " | " + texto);
        }
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
