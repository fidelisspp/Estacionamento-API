package com.joaovitor.estacionamento_api.exception;

public class ServicoEmUsoException extends RuntimeException {
    public ServicoEmUsoException(String message) {
        super(message);
    }
}
