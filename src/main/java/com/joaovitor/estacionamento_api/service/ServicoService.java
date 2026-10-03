package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.Servico;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.exception.NomeUnicoServicoException;
import com.joaovitor.estacionamento_api.exception.ServicoEmUsoException;
import com.joaovitor.estacionamento_api.repository.ServicoRepository;
import com.joaovitor.estacionamento_api.repository.projection.ServicoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ServicoService {

    private final ServicoRepository servicoRepository;

    @Transactional
    public Servico salvar(Servico servico) {
        try {
            return servicoRepository.save(servico);
        } catch (DataIntegrityViolationException ex) {
            throw new NomeUnicoServicoException(String.format("Serviço com nome '%s' já cadastrado",
                    servico.getNome()));
        }
    }

    @Transactional(readOnly = true)
    public Servico buscarPorId(Long id) {
        return servicoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Serviço id=%s não encontrado no sistema", id))
        );
    }

    @Transactional(readOnly = true)
    public Set<Servico> buscarPorIds(Collection<Long> ids) {
        Set<Long> idsUnicos = new HashSet<>(ids);
        Set<Servico> servicos = new HashSet<>(servicoRepository.findAllById(idsUnicos));

        if (servicos.size() != idsUnicos.size()) {
            servicos.forEach(servico -> idsUnicos.remove(servico.getId()));
            throw new EntityNotFoundException(String.format("Serviço(s) id=%s não encontrado(s) no sistema", idsUnicos));
        }
        return servicos;
    }

    @Transactional(readOnly = true)
    public Page<ServicoProjection> buscarTodos(Pageable pageable) {
        return servicoRepository.findAllPageable(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ServicoProjection> buscarPorTipo(Servico.TipoServico tipo, Pageable pageable) {
        return servicoRepository.findAllByTipo(tipo, pageable);
    }

    @Transactional
    public Servico atualizar(Long id, Servico dadosNovos) {
        Servico servico = buscarPorId(id);
        servico.setNome(dadosNovos.getNome());
        servico.setDescricao(dadosNovos.getDescricao());
        servico.setPreco(dadosNovos.getPreco());
        servico.setTipo(dadosNovos.getTipo());
        try {
            return servicoRepository.saveAndFlush(servico);
        } catch (DataIntegrityViolationException ex) {
            throw new NomeUnicoServicoException(String.format("Serviço com nome '%s' já cadastrado",
                    dadosNovos.getNome()));
        }
    }

    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarPorId(id);
        if (servicoRepository.isUsadoEmEstacionamento(id)) {
            throw new ServicoEmUsoException(String.format(
                    "Serviço id=%s não pode ser excluído, pois já foi usado em um estacionamento", id));
        }
        servicoRepository.delete(servico);
    }
}
