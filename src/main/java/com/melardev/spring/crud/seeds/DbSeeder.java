package com.melardev.spring.crud.seeds;


import com.github.javafaker.Faker;
import com.melardev.spring.crud.entities.Todo;
import com.melardev.spring.crud.repositories.TodosRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Locale;
import java.util.Set;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toSet;

@Component
public class DbSeeder implements CommandLineRunner {

    private final TodosRepository todosRepository;

    private final Faker faker;
    private final ReactiveMongoTemplate mongoTemplate;

    public DbSeeder(TodosRepository todosRepository, ReactiveMongoTemplate mongoTemplate) {
        this.todosRepository = todosRepository;
        faker = new Faker(Locale.getDefault());
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
/*
        for (String collectionName : mongoTemplate.getCollectionNames()) {
            if (collectionName.startsWith("todo")) {
                mongoTemplate.getCollection(collectionName).deleteMany((new BasicDBObject()));
            }
        }
*/
        int maxItemsToSeed = 32;
        Long currentTodosInDb = this.todosRepository.count().block();
        //long currentTodosInDb = 10;
        Set<Todo> todos = LongStream.range(currentTodosInDb, maxItemsToSeed)
                .mapToObj(i -> {
                    Todo todo = new Todo();
                    todo.setTitle(faker.lorem().sentence());
                    todo.setDescription(faker.lorem().paragraph());
                    todo.setCompleted(faker.random().nextBoolean());
                    return todo;
                })
                .collect(toSet());

        Flux<Todo> todoFlux = this.todosRepository.saveAll(todos);
        todoFlux.subscribe();

        // System.out.println(todoFlux.count().block());
        System.out.println("[+] " + (maxItemsToSeed - currentTodosInDb) + " todos seeded");

    }

}
