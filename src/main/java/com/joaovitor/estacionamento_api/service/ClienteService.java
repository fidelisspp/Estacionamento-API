package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.Cliente;
import com.joaovitor.estacionamento_api.exception.ClienteComHistoricoException;
import com.joaovitor.estacionamento_api.exception.CpfUniqueViolationException;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.repository.ClienteRepository;
import com.joaovitor.estacionamento_api.repository.projection.ClienteProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;

    @Transactional
    public Cliente salvar(Cliente cliente) {
        try {
            return clienteRepository.save(cliente);
        } catch (DataIntegrityViolationException ex) {
            throw new CpfUniqueViolationException(String.format("CPF '%s' não pode ser cadastrado, já existe no sistema", cliente.getCpf()));
        }
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente id=%s não encontrado no sistema", id))
        );
    }

    @Transactional(readOnly = true)
    public Page<ClienteProjection> buscarTodos(Pageable pageable) {
        return clienteRepository.findAllPageable(pageable);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorUsuarioId(Long id) {
        return clienteRepository.findByUsuarioId(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente do usuário id=%s não encontrado no sistema", id))
        );
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorCpf(String cpf) {
        return clienteRepository.findByCpf(cpf).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente com CPF '%s' não encontrado", cpf))
        );
    }

    @Transactional
    public Cliente atualizar(Long id, Cliente dadosNovos) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(dadosNovos.getNome());
        cliente.setCpf(dadosNovos.getCpf());
        try {
            return clienteRepository.saveAndFlush(cliente);
        } catch (DataIntegrityViolationException ex) {
            throw new CpfUniqueViolationException(String.format("CPF '%s' não pode ser cadastrado, já existe no sistema", dadosNovos.getCpf()));
        }
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        if (!cliente.getEstacionamentos().isEmpty()) {
            throw new ClienteComHistoricoException(String.format(
                    "Cliente id=%s não pode ser excluído, pois possui histórico de estacionamentos", id));
        }
        clienteRepository.delete(cliente);
    }
}
