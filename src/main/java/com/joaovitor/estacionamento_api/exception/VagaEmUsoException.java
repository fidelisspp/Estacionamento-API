package com.joaovitor.estacionamento_api.exception;

public class VagaEmUsoException extends RuntimeException {
    public VagaEmUsoException(String message) {
        super(message);
    }
}
