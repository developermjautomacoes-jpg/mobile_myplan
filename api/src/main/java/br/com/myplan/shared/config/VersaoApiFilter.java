package br.com.myplan.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class VersaoApiFilter extends OncePerRequestFilter {

    public static final String VERSAO_ATUAL = "2";
    public static final String FIM_DO_SUPORTE_V1 = "Wed, 30 Jun 2027 23:59:59 GMT";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.startsWith("/api/v1/")) {
            response.setHeader("API-Version", "1");
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", FIM_DO_SUPORTE_V1);
            response.setHeader("Link", "<" + uri.replaceFirst("/api/v1/", "/api/v2/") + ">; rel=\"successor-version\"");
        } else if (uri.startsWith("/api/v2/")) {
            response.setHeader("API-Version", "2");
        } else if (uri.startsWith("/api/tarefas")) {
            // endereço sem versão: continua funcionando, mas aponta para o endereço versionado
            response.setHeader("API-Version", VERSAO_ATUAL);
            response.setHeader("Deprecation", "true");
            response.setHeader("Link", "<" + uri.replaceFirst("/api/", "/api/v2/") + ">; rel=\"successor-version\"");
        }

        chain.doFilter(request, response);
    }
}
