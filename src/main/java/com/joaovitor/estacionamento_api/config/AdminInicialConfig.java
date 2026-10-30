package com.joaovitor.estacionamento_api.config;

import com.joaovitor.estacionamento_api.entity.Usuario;
import com.joaovitor.estacionamento_api.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "api.admin.inicial.enabled", havingValue = "true", matchIfMissing = true)
public class AdminInicialConfig {

    @Value("${api.admin.inicial.username:}")
    private String username;

    @Value("${api.admin.inicial.password:}")
    private String password;

    @Bean
    public ApplicationRunner criarAdminInicial(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.existsByRole(Usuario.Role.ROLE_ADMIN)) {
                return;
            }
            // mesma regra de tamanho do login (UsuarioLoginDto), senão o ADMIN não conseguiria se autenticar
            if (username.isBlank() || password.length() != 6) {
                throw new IllegalStateException(
                        "Nenhum ADMIN cadastrado: defina ADMIN_USERNAME (e-mail) e ADMIN_PASSWORD (exatamente 6 caracteres) para criar o administrador inicial");
            }
            if (usuarioRepository.findByUsername(username).isPresent()) {
                throw new IllegalStateException(
                        String.format("Nenhum ADMIN cadastrado e o usuário '%s' já existe como CLIENTE. Use outro ADMIN_USERNAME", username));
            }

            Usuario admin = new Usuario();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Usuario.Role.ROLE_ADMIN);
            usuarioRepository.save(admin);
            log.info("Administrador inicial '{}' criado", username);
        };
    }
}
