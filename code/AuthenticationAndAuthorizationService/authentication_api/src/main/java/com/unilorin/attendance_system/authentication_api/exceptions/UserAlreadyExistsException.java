package com.unilorin.attendance_system.authentication_api.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String string) {
        super(string);
    }
    public UserAlreadyExistsException() {
        super("User already exists");
    }
}
