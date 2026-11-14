package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.ChaveIdempotencia;
import com.joaovitor.estacionamento_api.repository.ChaveIdempotenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class IdempotenciaService {

    private static final Duration RETENCAO = Duration.ofHours(24);

    private final ChaveIdempotenciaRepository repository;

    @Transactional
    public Optional<ChaveIdempotencia> buscarValida(Long apiKeyId, String chave) {
        Optional<ChaveIdempotencia> registro = repository.findByApiKeyIdAndChave(apiKeyId, chave);
        if (registro.isPresent() && registro.get().getDataCriacao().isBefore(LocalDateTime.now().minus(RETENCAO))) {
            repository.delete(registro.get());
            repository.flush();
            return Optional.empty();
        }
        return registro;
    }

    @Transactional
    public ChaveIdempotencia reservar(Long apiKeyId, String chave, String metodo, String caminho, String hashCorpo) {
        ChaveIdempotencia registro = new ChaveIdempotencia();
        registro.setApiKeyId(apiKeyId);
        registro.setChave(chave);
        registro.setMetodo(metodo);
        registro.setCaminho(caminho);
        registro.setHashCorpo(hashCorpo);
        registro.setDataCriacao(LocalDateTime.now());
        return repository.saveAndFlush(registro);
    }

    @Transactional
    public void concluir(Long id, int status, String corpo, String contentType, String location) {
        repository.findById(id).ifPresent(registro -> {
            registro.setStatusResposta(status);
            registro.setCorpoResposta(corpo);
            registro.setContentType(contentType);
            registro.setLocation(location);
        });
    }

    @Transactional
    public void liberar(Long id) {
        repository.deleteById(id);
    }

    /**
     * Apaga, de hora em hora, os registros com mais de 24 horas (RETENCAO).
     * Sem isso, um registro vencido só seria apagado se a mesma chave fosse reenviada.
     * A primeira execução é 1 minuto depois de subir: no plano Free do Render a instância
     * hiberna quando fica parada, e pode não chegar a completar 1 hora no ar.
     */
    @Scheduled(fixedRate = 60, initialDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void apagarVencidas() {
        int apagadas = repository.apagarCriadasAntesDe(LocalDateTime.now().minus(RETENCAO));
        if (apagadas > 0) {
            log.info("Idempotência: {} registro(s) com mais de {} horas apagado(s)", apagadas, RETENCAO.toHours());
        }
    }
}
