package com.melardev.spring.crud.controllers;


import com.melardev.spring.crud.dtos.responses.*;
import com.melardev.spring.crud.entities.Todo;
import com.melardev.spring.crud.repositories.TodosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@CrossOrigin
@SuppressWarnings("Duplicates")
public class TodoRequestHandler {

    @Autowired
    TodosRepository todosRepository;


    public Mono<ServerResponse> getAll(ServerRequest request) {
        String pageStr = request.queryParam("page").orElse("1");
        String pageSizeStr = request.queryParam("page_size").orElse("5");
        int page = Integer.parseInt(pageStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        Pageable pageRequest = PageRequest.of(page - 1, pageSize);
        Flux<Todo> todos = todosRepository.findAllHqlSummary(pageRequest);

        Mono<AppResponse> response = getResponseFromTodosFlux(todos, todosRepository.count(), request, page, pageSize);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response, AppResponse.class);
    }


    public Mono<ServerResponse> getPending(ServerRequest request) {
        String pageStr = request.queryParam("page").orElse("1");
        String pageSizeStr = request.queryParam("page_size").orElse("5");
        int page = Integer.parseInt(pageStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        Pageable pageRequest = PageRequest.of(page - 1, pageSize);
        Flux<Todo> todos = todosRepository.findByCompletedFalse(pageRequest);
        Mono<AppResponse> response = getResponseFromTodosFlux(todos, todosRepository.countByCompletedIsFalse(), request, page, pageSize);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response, AppResponse.class);
    }


    public Mono<ServerResponse> getCompleted(ServerRequest request) {
        String pageStr = request.queryParam("page").orElse("1");
        String pageSizeStr = request.queryParam("page_size").orElse("5");
        int page = Integer.parseInt(pageStr);
        int pageSize = Integer.parseInt(pageSizeStr);

        Pageable pageRequest = PageRequest.of(page - 1, pageSize);
        Flux<Todo> todos = todosRepository.findByCompletedIsTrueHql(pageRequest);
        Mono<AppResponse> response = getResponseFromTodosFlux(todos, todosRepository.countByCompletedIsFalse(), request, page, pageSize);

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response, AppResponse.class);
    }


    public Mono<ServerResponse> getById(ServerRequest request) {
        String id = request.pathVariable("id");

        return this.todosRepository.findById(id)
                .flatMap((Function<Todo, Mono<ServerResponse>>) todo -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(new TodoDetailsResponse(todo)), TodoDetailsResponse.class))
                .switchIfEmpty(ServerResponse
                        .status(HttpStatus.NOT_FOUND)
                        .body(Mono.just(new ErrorResponse("Todo not found")), ErrorResponse.class));
    }


    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Todo> todo = request.bodyToMono(Todo.class);

        // Longer but more readable
        /*
        return todo.flatMap(new Function<Todo, Mono<Todo>>() {
            @Override
            public Mono<Todo> apply(Todo todo) {
                return todosRepository.save(todo);
            }
        }).flatMap(new Function<Todo, Mono<ServerResponse>>() {
            @Override
            public Mono<ServerResponse> apply(Todo todo) {
                return ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(new TodoDetailsResponse(savedTodo, "Todo Created Successfully")), AppResponse.class);
            }
        });
        */
        // Shorter way
        return todo.flatMap((Function<Todo, Mono<Todo>>) todoInput -> todosRepository.save(todoInput))
                .flatMap((Function<Todo, Mono<ServerResponse>>) savedTodo -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(new TodoDetailsResponse(savedTodo, "Todo Created Successfully")), AppResponse.class));
    }


    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Todo> todoInput = request.bodyToMono(Todo.class);

        /* Longer approach
        return todosRepository.findById(id)
                .flatMap(new Function<Todo, Mono<Todo>>() {
                    @Override
                    public Mono<Todo> apply(Todo todoFromDb) {
                        return todoInput.flatMap(new Function<Todo, Mono<Todo>>() {
                            @Override
                            public Mono<Todo> apply(Todo todo) {
                                String title = todo.getTitle();
                                if (title != null)
                                    todoFromDb.setTitle(title);

                                String description = todo.getDescription();
                                if (description != null)
                                    todoFromDb.setDescription(description);

                                todoFromDb.setCompleted(todo.isCompleted());
                                return todosRepository.save(todoFromDb);
                            }
                        });
                    }
                })
                .flatMap(new Function<Todo, Mono<ServerResponse>>() {
                    @Override
                    public Mono<ServerResponse> apply(Todo todo) {
                        return ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(new TodoDetailsResponse(todo, "Todo Updated")), TodoDetailsResponse.class);
                    }
                })
                .switchIfEmpty(ServerResponse
                        .status(HttpStatus.NOT_FOUND)
                        .body(Mono.just(new ErrorResponse("Todo not found")), ErrorResponse.class));
*/

        // Shorter but more readable
        return todosRepository.findById(id)
                .flatMap(t -> todoInput.flatMap((Function<Todo, Mono<Todo>>) todo -> {
                    String title = todo.getTitle();
                    if (title != null)
                        t.setTitle(title);

                    String description = todo.getDescription();
                    if (description != null)
                        t.setDescription(description);

                    t.setCompleted(todo.isCompleted());
                    return todosRepository.save(t);
                }))
                .flatMap(todo -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(new TodoDetailsResponse(todo, "Todo Updated")), TodoDetailsResponse.class))
                .switchIfEmpty(ServerResponse
                        .status(HttpStatus.NOT_FOUND)
                        .body(Mono.just(new ErrorResponse("Todo not found")), ErrorResponse.class));

    }

    @DeleteMapping("/{id}")
    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return todosRepository.findById(id)
                .flatMap(t -> todosRepository.delete(t)
                        .then(
                                ServerResponse.status(HttpStatus.CREATED)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(Mono.just(new SuccessResponse("Todo deleted")), SuccessResponse.class)
                        ))
                .switchIfEmpty(ServerResponse
                        .status(HttpStatus.NOT_FOUND)
                        .body(Mono.just(new ErrorResponse("Todo not found")), ErrorResponse.class));
    }


    @DeleteMapping
    public Mono<ServerResponse> deleteAll(ServerRequest request) {
        return todosRepository.deleteAll().then(ServerResponse.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new SuccessResponse("Todo deleted")), SuccessResponse.class));
    }

    private Mono<AppResponse> getResponseFromTodosFlux(Flux<Todo> todos, Mono<Long> count, ServerRequest request, int page, int pageSize) {
        // Less readable but less code, read the above if this one is hard to read, they are equivalent
        return todos.collectList().flatMap(todoList -> count
                .map(totalItemsCount -> PageMeta.build(todoList, request.uri().getPath(), page, pageSize, totalItemsCount))
                .map(pageMeta -> TodoListResponse.build(todoList, pageMeta)));

    }

}