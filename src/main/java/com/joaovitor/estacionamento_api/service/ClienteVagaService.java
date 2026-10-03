package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.ClienteVaga;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.exception.EstacionamentoEmAbertoException;
import com.joaovitor.estacionamento_api.jwt.JwtUserDetails;
import com.joaovitor.estacionamento_api.repository.ClienteVagaRepository;
import com.joaovitor.estacionamento_api.repository.projection.ClienteVagaProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ClienteVagaService {

    private final ClienteVagaRepository repository;

    @Transactional
    public ClienteVaga salvar(ClienteVaga clienteVaga) {
        return repository.save(clienteVaga);
    }

    @Transactional(readOnly = true)
    public ClienteVaga buscarPorRecibo(String recibo) {
        ClienteVaga cv = repository.findByReciboAndDataSaidaIsNull(recibo).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Recibo '%s' não encontrado no sistema ou check-out já realizado", recibo)
                )
        );
        verificarAcesso(cv);
        return cv;
    }

    @Transactional(readOnly = true)
    public ClienteVaga buscarPorReciboEmQualquerEstado(String recibo) {
        ClienteVaga cv = repository.findByRecibo(recibo).orElseThrow(
                () -> new EntityNotFoundException(String.format("Recibo '%s' não encontrado no sistema", recibo))
        );
        verificarAcesso(cv);
        return cv;
    }

    private void verificarAcesso(ClienteVaga cv) {
        JwtUserDetails jwtUserDetails = (JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean isAdmin = jwtUserDetails.getRole().equals("ROLE_ADMIN");
        boolean isClienteDonoDoRecibo = cv.getCliente().getUsuario().getUsername()
                .equals(jwtUserDetails.getUsername());

        if (!isAdmin && !isClienteDonoDoRecibo) {
            throw new AccessDeniedException("Acesso negado. Este recibo não pertence a você.");
        }
    }

    @Transactional(readOnly = true)
    public long getTotalDeVezesEstacionamentoCompleto(String cpf) {
        return repository.countByClienteCpfAndDataSaidaIsNotNull(cpf);
    }

    @Transactional(readOnly = true)
    public Page<ClienteVagaProjection> buscarTodosPorClienteCpf(String cpf, Pageable pageable) {
        return repository.findAllByClienteCpf(cpf, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ClienteVagaProjection> buscarTodosPorUsuarioId(Long id, Pageable pageable) {
        return repository.findAllByClienteUsuarioId(id, pageable);
    }

    @Transactional
    public void excluir(String recibo) {
        ClienteVaga cv = repository.findByRecibo(recibo).orElseThrow(
                () -> new EntityNotFoundException(String.format("Recibo '%s' não encontrado no sistema", recibo))
        );
        if (cv.getDataSaida() == null) {
            throw new EstacionamentoEmAbertoException(String.format(
                    "Estacionamento do recibo '%s' ainda está em aberto. Faça o check-out antes de excluir", recibo));
        }
        repository.delete(cv);
    }
}
