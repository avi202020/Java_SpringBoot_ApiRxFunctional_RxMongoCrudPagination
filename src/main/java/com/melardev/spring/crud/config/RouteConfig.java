package com.melardev.spring.crud.config;

import com.melardev.spring.crud.controllers.TodoRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouteConfig {

    @Bean
    public RouterFunction<ServerResponse> monoRouterFunction(TodoRequestHandler todoRequestHandler) {
        return route(GET("/api/todos").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::getAll)
                .andRoute(GET("/api/todos/pending").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::getPending)
                .andRoute(GET("/api/todos/completed").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::getCompleted)
                .andRoute(GET("/api/todos/{id}").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::getById)
                .andRoute(POST("/api/todos").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::create)
                .andRoute(PUT("/api/todos/{id}").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::update)
                .andRoute(DELETE("/api/todos/{id}").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::delete)
                .andRoute(DELETE("/api/todos").and(accept(MediaType.APPLICATION_JSON)), todoRequestHandler::deleteAll);
    }
}
