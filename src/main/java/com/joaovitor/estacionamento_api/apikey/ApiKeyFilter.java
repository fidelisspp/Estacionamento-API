package com.joaovitor.estacionamento_api.apikey;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String ATRIBUTO_API_KEY = "apiKey";

    private final ApiKeyService apiKeyService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || HttpMethod.OPTIONS.matches(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String chave = request.getHeader(HEADER_API_KEY);

        if (chave == null || chave.isBlank()) {
            responderNaoAutorizado(request, response, "Header X-API-Key é obrigatório");
            return;
        }

        Optional<ApiKey> apiKey = apiKeyService.buscarPorChave(chave.trim());

        if (apiKey.isEmpty() || !apiKey.get().isAtiva()) {
            responderNaoAutorizado(request, response, "Chave de API inválida ou inativa");
            return;
        }

        request.setAttribute(ATRIBUTO_API_KEY, apiKey.get());
        filterChain.doFilter(request, response);
    }

    private void responderNaoAutorizado(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String mensagem) throws IOException {
        log.info("Requisição recusada pela API Key: {} {} - {}",
                request.getMethod(), request.getRequestURI(), mensagem);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"path\":\"%s\",\"method\":\"%s\",\"status\":401,\"statusText\":\"Unauthorized\",\"message\":\"%s\"}",
                request.getRequestURI(), request.getMethod(), mensagem));
    }
}