package com.tasklist.auth.exception;

import javax.naming.AuthenticationException;

public class UserActivateException extends AuthenticationException {

    public UserActivateException(String explanation) {
        super(explanation);
    }
}
