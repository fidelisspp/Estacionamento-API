package com.joaovitor.estacionamento_api.entity;

import java.math.BigDecimal;

public enum Plano {

    FREE(10, new BigDecimal("0.00"), "Testes e desenvolvimento"),
    BASIC(60, new BigDecimal("60.00"), "Estacionamento pequeno, com uma unidade"),
    PRO(150, new BigDecimal("150.00"), "Redes com várias unidades e integrações");

    private final int limitePorMinuto;
    private final BigDecimal precoMensal;
    private final String descricao;

    Plano(int limitePorMinuto, BigDecimal precoMensal, String descricao) {
        this.limitePorMinuto = limitePorMinuto;
        this.precoMensal = precoMensal;
        this.descricao = descricao;
    }

    public int getLimitePorMinuto() {
        return limitePorMinuto;
    }

    public BigDecimal getPrecoMensal() {
        return precoMensal;
    }

    public String getDescricao() {
        return descricao;
    }
}