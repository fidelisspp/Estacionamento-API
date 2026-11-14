package com.joaovitor.estacionamento_api.idempotencia;

import com.joaovitor.estacionamento_api.apikey.ApiKeyFilter;
import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.ChaveIdempotencia;
import com.joaovitor.estacionamento_api.service.IdempotenciaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class IdempotenciaFilter extends OncePerRequestFilter {

    public static final String HEADER_IDEMPOTENCIA = "X-Idempotency-Key";
    public static final String HEADER_REPETIDA = "X-Idempotency-Replayed";
    private static final String CAMINHO_LOGIN = "/api/v1/auth";

    private final IdempotenciaService idempotenciaService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getRequestURI();
        return !HttpMethod.POST.matches(request.getMethod())
                || !caminho.startsWith("/api/")
                || caminho.equals(CAMINHO_LOGIN);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        ApiKey apiKey = (ApiKey) request.getAttribute(ApiKeyFilter.ATRIBUTO_API_KEY);
        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1. O header existe e é um UUID?
        String chave = request.getHeader(HEADER_IDEMPOTENCIA);
        if (chave == null || chave.isBlank()) {
            responderErro(request, response, 400, "Bad Request",
                    "Header X-Idempotency-Key é obrigatório em requisições POST");
            return;
        }
        chave = chave.trim();
        if (!ehUuid(chave)) {
            responderErro(request, response, 400, "Bad Request",
                    "Header X-Idempotency-Key deve ser um UUID (ex.: 3f2b8c1e-4a5d-4e6f-9a7b-1c2d3e4f5a6b)");
            return;
        }

        // 2. Lê o corpo uma vez e calcula a "impressão digital" (usuário + corpo)
        RequisicaoComCorpoEmCache requisicao = new RequisicaoComCorpoEmCache(request);
        String hash = calcularHash(usuarioAtual(), requisicao.getCorpo());
        String metodo = request.getMethod();
        String caminho = request.getRequestURI();

        // 3. A chave já foi usada?
        Optional<ChaveIdempotencia> existente = idempotenciaService.buscarValida(apiKey.getId(), chave);
        if (existente.isPresent()) {
            ChaveIdempotencia registro = existente.get();
            boolean mesmaRequisicao = registro.getMetodo().equals(metodo)
                    && registro.getCaminho().equals(caminho)
                    && registro.getHashCorpo().equals(hash);

            if (!mesmaRequisicao) {
                responderErro(request, response, 422, "Unprocessable Entity",
                        "A chave X-Idempotency-Key informada já foi usada em outra requisição (endpoint, corpo ou usuário diferente)");
                return;
            }
            if (registro.isEmProcessamento()) {
                responderErro(request, response, 409, "Conflict",
                        "Já existe uma requisição com esta X-Idempotency-Key em processamento. Tente novamente em instantes");
                return;
            }
            log.info("Idempotência: devolvendo resposta guardada para {} {}", metodo, caminho);
            responderComRespostaGuardada(registro, response);
            return;
        }

        // 4. Chave nova: reserva ANTES de processar
        ChaveIdempotencia reserva;
        try {
            reserva = idempotenciaService.reservar(apiKey.getId(), chave, metodo, caminho, hash);
        } catch (DataIntegrityViolationException e) {
            responderErro(request, response, 409, "Conflict",
                    "Já existe uma requisição com esta X-Idempotency-Key em processamento. Tente novamente em instantes");
            return;
        }

        // 5. Processa, capturando a resposta
        ContentCachingResponseWrapper resposta = new ContentCachingResponseWrapper(response);
        boolean concluida = false;
        try {
            filterChain.doFilter(requisicao, resposta);

            int status = resposta.getStatus();
            if (status >= 200 && status < 300) {
                String corpo = new String(resposta.getContentAsByteArray(), StandardCharsets.UTF_8);
                idempotenciaService.concluir(reserva.getId(), status, corpo,
                        resposta.getContentType(), resposta.getHeader(HttpHeaders.LOCATION));
                concluida = true;
            }
        } finally {
            if (!concluida) {
                idempotenciaService.liberar(reserva.getId());
            }
            resposta.copyBodyToResponse();
        }
    }

    private boolean ehUuid(String valor) {
        if (valor.length() != 36) {
            return false;
        }
        try {
            UUID.fromString(valor);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao != null ? autenticacao.getName() : "anonimo";
    }

    private String calcularHash(String usuario, byte[] corpo) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            sha256.update(usuario.getBytes(StandardCharsets.UTF_8));
            sha256.update((byte) '|');
            sha256.update(corpo);
            return HexFormat.of().formatHex(sha256.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    private void responderComRespostaGuardada(ChaveIdempotencia registro, HttpServletResponse response) throws IOException {
        response.setStatus(registro.getStatusResposta());
        if (registro.getContentType() != null) {
            response.setContentType(registro.getContentType());
        }
        if (registro.getLocation() != null) {
            response.setHeader(HttpHeaders.LOCATION, registro.getLocation());
        }
        response.setHeader(HEADER_REPETIDA, "true");
        if (registro.getCorpoResposta() != null) {
            response.getOutputStream().write(registro.getCorpoResposta().getBytes(StandardCharsets.UTF_8));
        }
    }

    private void responderErro(HttpServletRequest request, HttpServletResponse response,
                               int status, String statusText, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"path\":\"%s\",\"method\":\"%s\",\"status\":%d,\"statusText\":\"%s\",\"message\":\"%s\"}",
                request.getRequestURI(), request.getMethod(), status, statusText, mensagem));
    }
}
