package com.melardev.spring.crud.errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.melardev.spring.crud.dtos.responses.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Order(-2)
class RestWebExceptionHandler implements WebExceptionHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof ResourceNotFoundException) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        byte[] bytes = null;
        try {
            bytes = objectMapper.writeValueAsString(new ErrorResponse(ex.getMessage())).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        exchange.getResponse().getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Flux.just(buffer));
        // return Mono.error(ex);
    }
}