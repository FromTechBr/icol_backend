package com.br.org.icol.icolbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequisicaoNaoEncontrada.class)
    public ResponseEntity<ErroResponse> handleRequisicaoNaoEncontrada(RequisicaoNaoEncontrada ex) {
        ErroResponse erro = new ErroResponse(
            HttpStatus.NOT_FOUND.value(),
            "Não Encontrado",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(RequisicaoInvalida.class)
    public ResponseEntity<ErroResponse> handleRequisicaoInvalida(RequisicaoInvalida ex) {
        ErroResponse erro = new ErroResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Requisição Inválida",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagens = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));
            
        ErroResponse erro = new ErroResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Erro de Validação",
            mensagens,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleMensagemIlegivel(HttpMessageNotReadableException ex) {
        ErroResponse erro = new ErroResponse(
            HttpStatus.BAD_REQUEST.value(),
            "JSON Malformado",
            "A estrutura do JSON enviado está incorreta ou incompatível.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> handleRecursoNaoEncontrado(NoResourceFoundException ex) {
        ErroResponse erro = new ErroResponse(
            HttpStatus.NOT_FOUND.value(),
            "Não Encontrado",
            "O recurso solicitado não foi encontrado.",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleExceptionGenerica(Exception ex) {
        ErroResponse erro = new ErroResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erro Interno no Servidor",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
