package com.joaovitor.estacionamento_api.idempotencia;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RequisicaoComCorpoEmCache extends HttpServletRequestWrapper {

    private final byte[] corpo;

    public RequisicaoComCorpoEmCache(HttpServletRequest request) throws IOException {
        super(request);
        this.corpo = request.getInputStream().readAllBytes();
    }

    public byte[] getCorpo() {
        return corpo;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream copia = new ByteArrayInputStream(corpo);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return copia.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException("Leitura assíncrona não suportada");
            }

            @Override
            public int read() {
                return copia.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }
}
