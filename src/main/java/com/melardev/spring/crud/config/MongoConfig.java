package com.melardev.spring.crud.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

// @Configuration
// @EnableReactiveMongoRepositories
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Override
    public MongoClient reactiveMongoClient() {
        //return MongoClients.create("mongodb://localhost");
        return MongoClients.create();
    }

    @Override
    protected String getDatabaseName() {
        return "sboot_reactive_todos";
    }
}
