package com.br.org.icol.icolbackend.exception;

public class LimiteLoginExcedidoException extends RuntimeException {
    public LimiteLoginExcedidoException() {
        super("Muitas tentativas de login. Tente novamente mais tarde.");
    }
}
