package com.joaovitor.estacionamento_api.exception;

public class ClienteJaCadastradoException extends RuntimeException {
    public ClienteJaCadastradoException(String message) {
        super(message);
    }
}
