package com.tasklist.auth.controller;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.UserActivateException;
import com.tasklist.auth.object.JsonObject;
import com.tasklist.auth.service.UserService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/test")
    public ResponseEntity<User> getUser(@RequestBody Long id) throws NotFoundException {
        return ResponseEntity.ok(userService.findById(id));
    }

    // активация
    @PostMapping("/account-activate")
    public ResponseEntity<Boolean> activate(@RequestBody String uuid) throws UserActivateException {
        return ResponseEntity.ok(userService.activate(true, uuid) == 1);
    }

    // деактивация
    @PostMapping("/account-deactivate")
    public ResponseEntity<Boolean> deactivate(@RequestBody String uuid) throws UserActivateException {
        return ResponseEntity.ok(userService.activate(false, uuid) == 1);
    }

    // регистрация
    @PutMapping("/register")
    public ResponseEntity register(@Valid @RequestBody User user) throws AuthenticationException {
        userService.register(user);
        return ResponseEntity.ok().build(); //http OK - 200, регистрация прошла успешно
    }

    //Передача ошибки клиенту в формате Json
    //AuthenticationException.class - обработка только ошибок, связанных с аутентификацией
    @ExceptionHandler(AuthenticationException.class) //@ExceptionHandler позволяет перехватывать ошибки
    public ResponseEntity<JsonObject> handleException(AuthenticationException ex) {
        return new ResponseEntity(new JsonObject(ex.getClass().getSimpleName(), // передача типа ошибки
                ex.getMessage()), // передача текста ошибки
                HttpStatus.BAD_REQUEST);
    }
}
