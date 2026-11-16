package com.joaovitor.estacionamento_api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class SwaggerTermosDeUsoConfig {

    @Bean
    public SwaggerIndexTransformer swaggerIndexTransformer(SwaggerUiConfigProperties swaggerUiConfig,
                                                           SwaggerUiOAuthProperties swaggerUiOAuthProperties,
                                                           SwaggerWelcomeCommon swaggerWelcomeCommon,
                                                           ObjectMapperProvider objectMapperProvider) {
        return new TermosDeUsoTransformer(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon,
                objectMapperProvider);
    }

    static class TermosDeUsoTransformer extends SwaggerIndexPageTransformer {

        TermosDeUsoTransformer(SwaggerUiConfigProperties swaggerUiConfig,
                               SwaggerUiOAuthProperties swaggerUiOAuthProperties,
                               SwaggerWelcomeCommon swaggerWelcomeCommon,
                               ObjectMapperProvider objectMapperProvider) {
            super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
        }

        @Override
        public Resource transform(HttpServletRequest request, Resource resource,
                                  ResourceTransformerChain transformerChain) throws IOException {
            Resource transformado = super.transform(request, resource, transformerChain);
            if (!resource.getURL().toString().endsWith("swagger-initializer.js")) {
                return transformado;
            }
            String inicializador = ler(transformado);
            String termos = objectMapper.writeValueAsString(ler(new ClassPathResource("swagger/TERMOS-DE-USO.md")));
            String script = ler(new ClassPathResource("swagger/termos-de-uso.js"));
            String conteudo = inicializador + "\nwindow.TERMOS_DE_USO = " + termos + ";\n" + script;
            return new TransformedResource(transformado, conteudo.getBytes(StandardCharsets.UTF_8));
        }

        private String ler(Resource recurso) throws IOException {
            try (InputStream entrada = recurso.getInputStream()) {
                return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }
}
