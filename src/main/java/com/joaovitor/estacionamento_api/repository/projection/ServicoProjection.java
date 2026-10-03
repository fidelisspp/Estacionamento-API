package com.joaovitor.estacionamento_api.repository.projection;

import java.math.BigDecimal;

public interface ServicoProjection {

    Long getId();

    String getNome();

    String getDescricao();

    BigDecimal getPreco();

    String getTipo();
}
