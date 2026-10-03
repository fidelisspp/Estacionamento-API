package com.joaovitor.estacionamento_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "clientes_tem_vagas")
@EntityListeners(AuditingEntityListener.class)
public class ClienteVaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 15)
    @Column(name = "numero_recibo", nullable = false, unique = true, length = 15)
    private String recibo;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{4}$")
    @Column(name = "placa", nullable = false, length = 8)
    private String placa;

    @NotBlank
    @Size(max = 45)
    @Column(name = "marca", nullable = false, length = 45)
    private String marca;

    @NotBlank
    @Size(max = 45)
    @Column(name = "modelo", nullable = false, length = 45)
    private String modelo;

    @NotBlank
    @Size(max = 45)
    @Column(name = "cor", nullable = false, length = 45)
    private String cor;

    @NotNull
    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    @Column(name = "data_saida")
    private LocalDateTime dataSaida;

    @PositiveOrZero
    @Column(name = "valor", columnDefinition = "decimal(10,2)")
    private BigDecimal valor;

    @PositiveOrZero
    @Column(name = "desconto", columnDefinition = "decimal(10,2)")
    private BigDecimal desconto;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_vaga", nullable = false)
    private Vaga vaga;

    @ManyToMany
    @JoinTable(name = "clientes_vagas_servicos",
            joinColumns = @JoinColumn(name = "id_cliente_vaga"),
            inverseJoinColumns = @JoinColumn(name = "id_servico"))
    private Set<Servico> servicos = new HashSet<>();

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClienteVaga that = (ClienteVaga) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
