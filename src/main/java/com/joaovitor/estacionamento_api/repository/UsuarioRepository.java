package com.joaovitor.estacionamento_api.repository;

import com.joaovitor.estacionamento_api.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    @Query("select u.role from Usuario u where u.username = :username")
    Usuario.Role findRoleByUsername(String username);

    Page<Usuario> findAllByRole(Usuario.Role role, Pageable pageable);

    @Query("select count(c) > 0 from Cliente c where c.usuario.id = :id")
    boolean isVinculadoACliente(@Param("id") Long id);
}
