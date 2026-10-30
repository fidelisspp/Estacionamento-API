package com.joaovitor.estacionamento_api.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtSecretConfig {

    public JwtSecretConfig(@Value("${api.security.jwt.secret:}") String secret) {
        JwtUtils.configureSecretKey(secret);
    }
}
