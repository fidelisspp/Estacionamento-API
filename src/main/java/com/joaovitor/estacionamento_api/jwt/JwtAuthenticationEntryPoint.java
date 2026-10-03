package com.joaovitor.estacionamento_api.jwt;

import com.joaovitor.estacionamento_api.web.exception.ErrorMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        log.info("Http Status 401 {}", authException.getMessage());

        ErrorMessage error = new ErrorMessage(request, HttpStatus.UNAUTHORIZED, "Autenticação necessária");

        response.setHeader("www-authenticate", "Bearer realm='/api/v1/auth'");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
}
