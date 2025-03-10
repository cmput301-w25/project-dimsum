package com.example.baobook.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String username) {
        super("Username not found: " + username);
    }
}
