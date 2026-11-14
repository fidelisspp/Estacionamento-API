package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.ChaveIdempotencia;
import com.joaovitor.estacionamento_api.repository.ChaveIdempotenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

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
}
