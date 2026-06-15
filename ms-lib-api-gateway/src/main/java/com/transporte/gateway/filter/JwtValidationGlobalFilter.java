package com.transporte.gateway.filter;

import com.transporte.gateway.service.JwtValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class JwtValidationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtValidationService jwtValidationService;

    public JwtValidationGlobalFilter(JwtValidationService jwtValidationService) {
        this.jwtValidationService = jwtValidationService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Rutas públicas que no requieren JWT
        if (isPublicRoute(path)) {
            return chain.filter(exchange);
        }

        // Validar IP si es administración
        if (path.startsWith("/admin")) {
            if (!jwtValidationService.isIpWhitelisted(getClientIp(exchange))) {
                log.warn("IP no autorizada para admin: {}", getClientIp(exchange));
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        }

        String token = extractToken(exchange);

        if (!StringUtils.hasText(token)) {
            log.warn("Token no encontrado en la solicitud para: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (!jwtValidationService.validateToken(token)) {
            log.warn("Token inválido para: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isPublicRoute(String path) {
        return path.startsWith("/auth/") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        return exchange.getRequest().getRemoteAddress().getHostName();
    }

    @Override
    public int getOrder() {
        return -1; // Alta prioridad
    }
}
