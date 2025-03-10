package com.example.baobook.exception;

public class UsernameAlreadyExistsException extends Exception {
    public UsernameAlreadyExistsException(String username) {
        super("Attempted to register a user with a taken username: " + username);
    }
}
