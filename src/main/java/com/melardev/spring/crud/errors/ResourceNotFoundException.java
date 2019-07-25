package com.melardev.spring.crud.errors;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String id) {
        super("Todo:" + id + " is not found.");
    }
}