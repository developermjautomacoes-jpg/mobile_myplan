package br.com.myplan.shared.exception;

import br.com.myplan.shared.config.RequisicaoFilter;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Pattern VERSAO_NA_URL = Pattern.compile("^/api/(v\\d+)(/.*)?$");

    // exceções da própria API (tarefa não encontrada, duplicada, regra de negócio, parâmetro inválido)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErroResponse> erroDaApi(ApiException ex, HttpServletRequest req) {
        return erro(ex.getCodigo(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        Matcher m = VERSAO_NA_URL.matcher(req.getRequestURI());
        if (m.matches() && !m.group(1).equals("v1") && !m.group(1).equals("v2")) {
            return erro(CodigoErro.VERSAO_NAO_SUPORTADA,
                    "A versão " + m.group(1) + " não existe", req, null);
        }
        return erro(CodigoErro.ROTA_NAO_ENCONTRADA,
                "O endereço " + req.getRequestURI() + " não existe nesta API", req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        String detalhe = campos.size() == 1
                ? "1 campo está inválido"
                : campos.size() + " campos estão inválidos";
        return erro(CodigoErro.DADOS_INVALIDOS, detalhe, req, campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> parametroForaDaRegra(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String caminho = v.getPropertyPath().toString();
            campos.putIfAbsent(caminho.substring(caminho.lastIndexOf('.') + 1), v.getMessage());
        });
        return erro(CodigoErro.PARAMETRO_INVALIDO, "Um ou mais parâmetros da URL estão inválidos", req, campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> parametroForaDaRegra(HandlerMethodValidationException ex, HttpServletRequest req) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getParameterValidationResults().forEach(r -> {
            String nome = r.getMethodParameter().getParameterName();
            r.getResolvableErrors().forEach(e -> campos.putIfAbsent(nome, e.getDefaultMessage()));
        });
        return erro(CodigoErro.PARAMETRO_INVALIDO, "Um ou mais parâmetros da URL estão inválidos", req, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        String detalhe = "O corpo da requisição não é um JSON válido";

        if (ex.getCause() instanceof UnrecognizedPropertyException e) {
            detalhe = "O campo '" + e.getPropertyName() + "' não existe na tarefa e não pode ser enviado";
        } else if (ex.getCause() instanceof InvalidFormatException e && !e.getPath().isEmpty()) {
            String campo = e.getPath().get(e.getPath().size() - 1).getFieldName();
            Class<?> tipo = e.getTargetType();
            if (tipo.isEnum()) {
                detalhe = "O valor '" + e.getValue() + "' não é válido para '" + campo + "'. Valores aceitos: "
                        + Arrays.toString(tipo.getEnumConstants());
            } else if (tipo.getSimpleName().equals("LocalDate")) {
                detalhe = "A data '" + e.getValue() + "' em '" + campo + "' está fora do formato. Use aaaa-mm-dd, por exemplo 2026-12-10";
            } else {
                detalhe = "O valor '" + e.getValue() + "' não é válido para '" + campo + "'";
            }
        }
        return erro(CodigoErro.CORPO_INVALIDO, detalhe, req, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String detalhe = "O valor '" + ex.getValue() + "' não é válido para '" + ex.getName() + "'";
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            detalhe += ". Valores aceitos: " + Arrays.toString(ex.getRequiredType().getEnumConstants());
        } else if (ex.getRequiredType() == Long.class) {
            detalhe += ". Informe um número inteiro";
        }
        return erro(CodigoErro.PARAMETRO_INVALIDO, detalhe, req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        String permitidos = ex.getSupportedHttpMethods() == null ? "" : ex.getSupportedHttpMethods().toString();
        ResponseEntity<ErroResponse> resposta = erro(CodigoErro.METODO_NAO_SUPORTADO,
                "O método " + ex.getMethod() + " não é aceito em " + req.getRequestURI()
                        + (permitidos.isEmpty() ? "" : ". Métodos aceitos: " + permitidos), req, null);
        if (ex.getSupportedHttpMethods() != null) {
            return ResponseEntity.status(resposta.getStatusCode())
                    .headers(resposta.getHeaders())
                    .allow(ex.getSupportedHttpMethods().toArray(org.springframework.http.HttpMethod[]::new))
                    .body(resposta.getBody());
        }
        return resposta;
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponse> tipoConteudo(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        String recebido = ex.getContentType() == null ? "nenhum" : ex.getContentType().toString();
        return erro(CodigoErro.TIPO_DE_CONTEUDO_INVALIDO,
                "O Content-Type recebido foi '" + recebido + "', mas este endpoint só aceita application/json", req, null);
    }

    // qualquer erro não previsto: registra tudo no log e devolve uma mensagem genérica, sem detalhes internos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> erroInterno(Exception ex, HttpServletRequest req) {
        log.error("[{}] Erro inesperado em {} {}", requestId(req), req.getMethod(), req.getRequestURI(), ex);
        return erro(CodigoErro.ERRO_INTERNO, "Ocorreu um erro inesperado ao processar a requisição", req, null);
    }

    private ResponseEntity<ErroResponse> erro(CodigoErro codigo, String detalhe,
                                              HttpServletRequest req, Map<String, String> campos) {
        if (codigo != CodigoErro.ERRO_INTERNO) {
            log.warn("[{}] {} {} -> {} {}: {}", requestId(req), req.getMethod(), req.getRequestURI(),
                    codigo.getStatus().value(), codigo, detalhe);
        }

        ErroResponse body = new ErroResponse(
                codigo.getTipo(),
                codigo.getTitulo(),
                codigo.getStatus().value(),
                detalhe,
                req.getRequestURI(),
                codigo.name(),
                codigo.getDica(),
                campos == null || campos.isEmpty() ? null : campos,
                requestId(req),
                LocalDateTime.now());

        return ResponseEntity.status(codigo.getStatus())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .body(body);
    }

    private String requestId(HttpServletRequest req) {
        Object id = req.getAttribute(RequisicaoFilter.ATRIBUTO_REQUEST_ID);
        return id == null ? null : id.toString();
    }
}
