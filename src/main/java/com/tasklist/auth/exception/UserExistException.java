package com.tasklist.auth.exception;

import javax.naming.AuthenticationException;

public class UserExistException extends AuthenticationException {

    public UserExistException(String message) {
        super(message);
    }
}
