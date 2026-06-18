package com.travelbuddy.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Recursos estáticos inexistentes (ex: favicon.ico) — 404 silencioso
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Erro não tratado: {}", ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(Map.of(
                        "erro", ex.getMessage(),
                        "tipo", ex.getClass().getSimpleName()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Erro interno", ex);
        return ResponseEntity
                .internalServerError()
                .body(Map.of("erro", "Erro interno do servidor"));
    }
}
