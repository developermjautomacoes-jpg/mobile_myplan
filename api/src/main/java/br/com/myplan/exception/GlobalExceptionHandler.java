package br.com.myplan.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TarefaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(TarefaNaoEncontradaException ex, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, "TAREFA_NAO_ENCONTRADA", ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return erro(HttpStatus.NOT_FOUND, "ROTA_NAO_ENCONTRADA",
                "O endereço " + req.getRequestURI() + " não existe nesta API", req, null);
    }

    @ExceptionHandler(TarefaDuplicadaException.class)
    public ResponseEntity<ErroResponse> duplicada(TarefaDuplicadaException ex, HttpServletRequest req) {
        return erro(HttpStatus.CONFLICT, "TAREFA_DUPLICADA", ex.getMessage(), req, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return erro(HttpStatus.UNPROCESSABLE_ENTITY, "REGRA_DE_NEGOCIO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return erro(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Um ou mais campos estão inválidos", req, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> parametroForaDaRegra(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String caminho = v.getPropertyPath().toString();
            String nome = caminho.substring(caminho.lastIndexOf('.') + 1);
            campos.putIfAbsent(nome, v.getMessage());
        });
        return erro(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Um ou mais parâmetros estão inválidos", req, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> parametroForaDaRegra(HandlerMethodValidationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(r -> {
            String nome = r.getMethodParameter().getParameterName();
            r.getResolvableErrors().forEach(e -> campos.putIfAbsent(nome, e.getDefaultMessage()));
        });
        return erro(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", "Um ou mais parâmetros estão inválidos", req, campos);
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(ParametroInvalidoException ex, HttpServletRequest req) {
        return erro(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", ex.getMessage(), req, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String mensagem = "Corpo da requisição inválido ou mal formado";

        if (ex.getCause() instanceof UnrecognizedPropertyException e) {
            mensagem = "O campo '" + e.getPropertyName() + "' não é aceito";
        } else if (ex.getCause() instanceof InvalidFormatException e && !e.getPath().isEmpty()) {
            String campo = e.getPath().get(e.getPath().size() - 1).getFieldName();
            Class<?> tipo = e.getTargetType();
            if (tipo.isEnum()) {
                mensagem = "Valor '" + e.getValue() + "' inválido para '" + campo + "'. Valores aceitos: "
                        + Arrays.toString(tipo.getEnumConstants());
            } else {
                mensagem = "Valor '" + e.getValue() + "' inválido para '" + campo + "'"
                        + (tipo.getSimpleName().equals("LocalDate") ? ". Use o formato aaaa-mm-dd" : "");
            }
        }
        return erro(HttpStatus.BAD_REQUEST, "CORPO_INVALIDO", mensagem, req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String mensagem = "Valor '" + ex.getValue() + "' inválido para '" + ex.getName() + "'";
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            mensagem += ". Valores aceitos: " + Arrays.toString(ex.getRequiredType().getEnumConstants());
        }
        return erro(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", mensagem, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NAO_SUPORTADO",
                "O método " + ex.getMethod() + " não é suportado neste endereço", req, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoConteudo(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_DE_CONTEUDO_INVALIDO",
                "Envie o corpo em JSON (Content-Type: application/json)", req, null);
    }

    // qualquer erro não previsto: registra no log e não expõe detalhes internos para o cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInterno(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro inesperado. Tente novamente mais tarde", req, null);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String codigo, String mensagem,
                                              HttpServletRequest req, Map<String, String> campos) {
        ErroResponse body = new ErroResponse(LocalDateTime.now(), status.value(),
                status.getReasonPhrase(), codigo, mensagem, req.getRequestURI(), campos);
        return ResponseEntity.status(status).body(body);
    }
}
