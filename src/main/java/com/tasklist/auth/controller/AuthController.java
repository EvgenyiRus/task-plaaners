package com.tasklist.auth.controller;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.service.UserService;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/test")
    public ResponseEntity<User> getUser (@RequestBody Long id) throws NotFoundException {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/register")
    public ResponseEntity add(@Valid @RequestBody User user) throws UserExistException {
        userService.saveOrUpdate(user);
        return ResponseEntity.ok().build(); //http OK - 200, регистрация прошла успешно
    }
}
