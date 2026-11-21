package br.com.myplan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequisicaoFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequisicaoFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String id = request.getHeader("X-Request-Id");
        if (id == null || !id.matches("[A-Za-z0-9-]{1,64}")) {
            id = UUID.randomUUID().toString();
        }
        response.setHeader("X-Request-Id", id);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "no-store");

        long inicio = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long tempo = System.currentTimeMillis() - inicio;
            log.info("[{}] {} {} -> {} ({} ms)", id, request.getMethod(), request.getRequestURI(),
                    response.getStatus(), tempo);
        }
    }
}
