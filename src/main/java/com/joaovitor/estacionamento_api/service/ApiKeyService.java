package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.ApiKey;
import com.joaovitor.estacionamento_api.entity.Plano;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.exception.UltimaApiKeyAtivaException;
import com.joaovitor.estacionamento_api.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    @Transactional
    public ApiKey criar(String nome, Plano plano) {
        return criarComChave(nome, plano, UUID.randomUUID().toString());
    }

    @Transactional
    public ApiKey criarComChave(String nome, Plano plano, String chave) {
        ApiKey apiKey = new ApiKey();
        apiKey.setNome(nome);
        apiKey.setPlano(plano);
        apiKey.setChave(chave);
        return apiKeyRepository.save(apiKey);
    }

    @Transactional(readOnly = true)
    public Page<ApiKey> buscarTodas(Pageable pageable) {
        return apiKeyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ApiKey> buscarPorChave(String chave) {
        return apiKeyRepository.findByChave(chave);
    }

    @Transactional(readOnly = true)
    public ApiKey buscarPorId(Long id) {
        return apiKeyRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Chave de API id=%s não encontrada no sistema", id))
        );
    }

    @Transactional
    public ApiKey trocarPlano(Long id, Plano novoPlano) {
        ApiKey apiKey = buscarPorId(id);
        apiKey.setPlano(novoPlano);
        return apiKey;
    }

    @Transactional
    public void desativar(Long id) {
        ApiKey apiKey = buscarPorId(id);
        if (apiKey.isAtiva() && apiKeyRepository.countByAtivaTrue() <= 1) {
            throw new UltimaApiKeyAtivaException(String.format(
                    "Chave de API id=%s não pode ser desativada, pois é a última chave ativa", id));
        }
        apiKey.setAtiva(false);
    }
}