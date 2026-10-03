package com.joaovitor.estacionamento_api.exception;

public class EstacionamentoEmAbertoException extends RuntimeException {
    public EstacionamentoEmAbertoException(String message) {
        super(message);
    }
}
