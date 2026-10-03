package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.Vaga;
import com.joaovitor.estacionamento_api.exception.CodigoUnicoVagaException;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.exception.VagaEmUsoException;
import com.joaovitor.estacionamento_api.repository.VagaRepository;
import com.joaovitor.estacionamento_api.repository.projection.VagaProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class VagaService {

    private final VagaRepository vagaRepository;

    public Vaga salvar(Vaga vaga) {
        try {
            return vagaRepository.save(vaga);
        } catch (DataIntegrityViolationException ex) {
            throw new CodigoUnicoVagaException(String.format("Vaga com código '%s' já cadastrada",
                    vaga.getCodigo()));
        }
    }

    @Transactional(readOnly = true)
    public Vaga buscarPorCodigo(String codigo) {
        return vagaRepository.findByCodigo(codigo).orElseThrow(
                () -> new EntityNotFoundException(String.format("Vaga com código '%s' não foi encontrada", codigo))
        );
    }

    @Transactional(readOnly = true)
    public Vaga buscarPorVagaLivre() {
        return vagaRepository.findFirstByStatus(Vaga.StatusVaga.LIVRE).orElseThrow(
                () -> new EntityNotFoundException("Nenhuma vaga livre foi encontrada")
        );
    }

    @Transactional(readOnly = true)
    public Page<VagaProjection> buscarTodos(Pageable pageable) {
        return vagaRepository.findAllPageable(pageable);
    }

    @Transactional(readOnly = true)
    public Page<VagaProjection> buscarPorStatus(Vaga.StatusVaga status, Pageable pageable) {
        return vagaRepository.findAllByStatus(status, pageable);
    }

    @Transactional
    public Vaga atualizar(String codigo, Vaga dadosNovos) {
        Vaga vaga = buscarPorCodigo(codigo);
        if (vagaRepository.isComEstacionamentoEmAberto(vaga.getId())) {
            throw new VagaEmUsoException(String.format(
                    "Vaga '%s' não pode ser alterada, pois tem um veículo estacionado", codigo));
        }
        vaga.setCodigo(dadosNovos.getCodigo());
        vaga.setStatus(dadosNovos.getStatus());
        try {
            return vagaRepository.saveAndFlush(vaga);
        } catch (DataIntegrityViolationException ex) {
            throw new CodigoUnicoVagaException(String.format("Vaga com código '%s' já cadastrada",
                    dadosNovos.getCodigo()));
        }
    }

    @Transactional
    public void excluir(String codigo) {
        Vaga vaga = buscarPorCodigo(codigo);
        if (vagaRepository.isUsadaEmEstacionamento(vaga.getId())) {
            throw new VagaEmUsoException(String.format(
                    "Vaga '%s' não pode ser excluída, pois já foi usada em um estacionamento", codigo));
        }
        vagaRepository.delete(vaga);
    }
}
