package com.joaovitor.estacionamento_api.apikey;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    public static final String HEADER_LIMIT = "X-Rate-Limit-Limit";
    public static final String HEADER_REMAINING = "X-Rate-Limit-Remaining";
    public static final String HEADER_RETRY_AFTER = "Retry-After";

    private final RateLimitService rateLimitService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/")
                || HttpMethod.OPTIONS.matches(request.getMethod());
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

        ConsumptionProbe probe = rateLimitService.consumir(apiKey);
        int limite = apiKey.getPlano().getLimitePorMinuto();

        response.setHeader(HEADER_LIMIT, String.valueOf(limite));

        if (probe.isConsumed()) {
            response.setHeader(HEADER_REMAINING, String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long segundos = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill() + 999_999_999L));

        log.info("Rate limit excedido: plano {} - {} {}", apiKey.getPlano(), request.getMethod(), request.getRequestURI());

        response.setHeader(HEADER_REMAINING, "0");
        response.setHeader(HEADER_RETRY_AFTER, String.valueOf(segundos));
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"path\":\"%s\",\"method\":\"%s\",\"status\":429,\"statusText\":\"Too Many Requests\"," +
                        "\"message\":\"Limite do plano %s (%d requisições por minuto) excedido. Tente novamente em %d segundo(s).\"}",
                request.getRequestURI(), request.getMethod(), apiKey.getPlano(), limite, segundos));
    }
}
