package com.tasklist.auth.exception;

import javax.naming.AuthenticationException;

public class RoleExistException extends AuthenticationException {

    public RoleExistException(String message) {
        super(message);
    }
}
