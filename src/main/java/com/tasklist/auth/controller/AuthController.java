package com.tasklist.auth.controller;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.RoleExistException;
import com.tasklist.auth.exception.UserActivateException;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.object.JsonObject;
import com.tasklist.auth.service.UserDetailsImpl;
import com.tasklist.auth.service.UserService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/test")
    public String test() {
        return "test ok";
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
    public ResponseEntity<User> register(@Valid @RequestBody User user) throws UserExistException, RoleExistException {
        userService.register(user);
        return ResponseEntity.ok().build(); //http OK - 200, регистрация прошла успешно
    }

    // авторизация
    // https://medium.com/geekculture/spring-security-authentication-process-authentication-flow-behind-the-scenes-d56da63f04fa
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) {
        // получение данных пользователя после успешной аутентификации
        UserDetailsImpl userDetails = userService.login(user);
        // проверка на активацию пользователя
        if(!userDetails.getUser().getActivity().isActivated()) {
            throw new DisabledException("User not activate");
        }
        return ResponseEntity.ok().body(userDetails.getUser());
    }

    // передача ошибки клиенту в формате Json
    // AuthenticationException.class - обработка только ошибок, связанных с аутентификацией
    // Exception - всех ошибок
    @ExceptionHandler(Exception.class) //@ExceptionHandler позволяет перехватывать ошибки
    public ResponseEntity<JsonObject> handleException(Exception ex) {
        /*
        DisabledException - не активирован
        UserAlreadyActivatedException - пользователь уже активирован (пытается неск. раз активировать)
        UsernameNotFoundException - username или email не найден в базе

        BadCredentialsException - неверные данные пользователя
        UserOrEmailExistsException - пользователь или email уже существуют
        DataIntegrityViolationException - ошибка уникальности в БД

        Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)
        */

        return new ResponseEntity<>(new JsonObject(ex.getClass().getSimpleName(), // передача типа ошибки
                ex.getMessage()),
                 // передача текста ошибки
                HttpStatus.BAD_REQUEST);
    }
}
