package com.joaovitor.estacionamento_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "servicos")
@EntityListeners(AuditingEntityListener.class)
public class Servico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 60)
    @Column(name = "nome", nullable = false, unique = true, length = 60)
    private String nome;

    @Size(max = 255)
    @Column(name = "descricao", length = 255)
    private String descricao;

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    @Column(name = "preco", nullable = false, columnDefinition = "decimal(10,2)")
    private BigDecimal preco;

    @NotNull
    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoServico tipo;

    @CreatedDate
    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_modificacao")
    private LocalDateTime dataModificacao;

    @CreatedBy
    @Column(name = "criado_por")
    private String criadoPor;

    @LastModifiedBy
    @Column(name = "modificado_por")
    private String modificadoPor;

    public enum TipoServico {
        LAVAGEM, MANOBRISTA, CALIBRAGEM, OUTRO
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Servico servico = (Servico) o;
        return Objects.equals(id, servico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
