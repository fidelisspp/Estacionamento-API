package com.joaovitor.estacionamento_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "chaves_idempotencia",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotencia_api_key_chave",
                columnNames = {"id_api_key", "chave"}))
public class ChaveIdempotencia implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 36)
    @Column(name = "chave", nullable = false, length = 36)
    private String chave;

    @NotNull
    @Column(name = "id_api_key", nullable = false)
    private Long apiKeyId;

    @NotBlank
    @Column(name = "metodo", nullable = false, length = 10)
    private String metodo;

    @NotBlank
    @Column(name = "caminho", nullable = false, length = 255)
    private String caminho;

    @NotBlank
    @Size(max = 64)
    @Column(name = "hash_corpo", nullable = false, length = 64)
    private String hashCorpo;

    @Column(name = "status_resposta")
    private Integer statusResposta;

    @Lob
    @Column(name = "corpo_resposta")
    private String corpoResposta;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "location", length = 255)
    private String location;

    @NotNull
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    public boolean isEmProcessamento() {
        return statusResposta == null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChaveIdempotencia that = (ChaveIdempotencia) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
