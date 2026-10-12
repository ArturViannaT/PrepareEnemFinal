package com.atividadeAvaliativaLP3.demo.excecao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Tratamento de erros de validação de campos (@NotBlank, @NotNull, etc. - RNF5 e RNF6)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErrosValidacao(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Erro de Preenchimento");

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        response.put("messages", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Tratamento de credenciais inválidas (RNF4)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> tratarCredenciaisIncorretas(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Falha de Autenticação");
        response.put("message", "E-mail ou senha incorretos"); // RNF4

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Tratamento de usuário não encontrado (RNF4)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> tratarUsuarioNaoEncontrado(UsernameNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Falha de Autenticação");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Exceções de Regra de Negócio (ex: e-mail duplicado, validação de domínio acadêmico)
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> tratarRegraNegocio(RegraNegocioException ex) {        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Regra de Negócio Violada");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Recurso não encontrado
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Não Encontrado");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // Foto de perfil: arquivo inválido (tipo/tamanho) -> 400 com mensagem amigável
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Arquivo Inválido");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Foto de perfil: Storage não configurado no servidor -> 503 (frontend usa fallback direto)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> tratarEstadoIlegal(IllegalStateException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Serviço Indisponível");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // Tratamento genérico para qualquer outra exceção inesperada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarQualquerExcecao(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Erro Interno do Servidor");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
