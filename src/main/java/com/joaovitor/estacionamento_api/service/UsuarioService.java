package com.joaovitor.estacionamento_api.service;

import com.joaovitor.estacionamento_api.entity.Usuario;
import com.joaovitor.estacionamento_api.exception.UsernameUniqueViolationException;
import com.joaovitor.estacionamento_api.repository.UsuarioRepository;
import com.joaovitor.estacionamento_api.exception.EntityNotFoundException;
import com.joaovitor.estacionamento_api.exception.PasswordInvalidException;
import com.joaovitor.estacionamento_api.exception.ParametroInvalidoException;
import com.joaovitor.estacionamento_api.exception.UsuarioExclusaoInvalidaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario salvar(Usuario usuario) {
        try {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            return usuarioRepository.save(usuario);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new UsernameUniqueViolationException(String.format("Username {%s} já cadastrado", usuario.getUsername()));
        }

    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário id=%s não encontrado", id))
        );
    }

    @Transactional
    public Usuario editarSenha(Long id, String senhaAtual, String novaSenha, String confirmaSenha) {
        if(!novaSenha.equals(confirmaSenha)) {
            throw new PasswordInvalidException("Nova senha não confere com confirmação de senha.");
        }

        Usuario user = buscarPorId(id);
        if(!passwordEncoder.matches(senhaAtual, user.getPassword())) {
            throw new PasswordInvalidException("Sua senha não confere");
        }

        user.setPassword(passwordEncoder.encode(novaSenha));
        return user;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> buscarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Usuario> buscarPorRole(String perfil, Pageable pageable) {
        Usuario.Role role;
        try {
            role = Usuario.Role.valueOf("ROLE_" + perfil.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ParametroInvalidoException(String.format(
                    "Perfil '%s' inválido. Valores aceitos: ADMIN, CLIENTE", perfil));
        }
        return usuarioRepository.findAllByRole(role, pageable);
    }

    @Transactional
    public void excluir(Long id, Long idUsuarioLogado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getId().equals(idUsuarioLogado)) {
            throw new UsuarioExclusaoInvalidaException("Não é permitido excluir o próprio usuário");
        }
        if (usuarioRepository.isVinculadoACliente(id)) {
            throw new UsuarioExclusaoInvalidaException(String.format(
                    "Usuário id=%s não pode ser excluído, pois possui cadastro de cliente. Exclua o cliente antes", id));
        }
        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com '%s' não encontrado", username))
        );
    }

    @Transactional(readOnly = true)
    public Usuario.Role buscarRolePorUsername(String username) {
        return usuarioRepository.findRoleByUsername(username);
    }
}
