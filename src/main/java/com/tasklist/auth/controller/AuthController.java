package com.tasklist.auth.controller;

import com.tasklist.auth.entity.Activity;
import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.RoleExistException;
import com.tasklist.auth.exception.UserActivateException;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.object.JsonException;
import com.tasklist.auth.service.EmailService;
import com.tasklist.auth.service.UserDetailsImpl;
import com.tasklist.auth.service.UserDetailsServiceImpl;
import com.tasklist.auth.service.UserService;
import com.tasklist.auth.utils.CookieUtils;
import com.tasklist.auth.utils.JwtUtils;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final UserDetailsServiceImpl userDetailsService; // для поиска пользователя и добавления его в Spring контейнер

    @Autowired
    public AuthController(UserService userService, EmailService emailService, JwtUtils jwtUtils,
                          CookieUtils cookieUtils, UserDetailsServiceImpl userDetailsService) {
        this.userService = userService;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.cookieUtils = cookieUtils;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/test")
    public String test() {
        return "test ok";
    }

    @PostMapping("/test")
    public ResponseEntity<User> getUser(@NotNull @RequestBody Long id) throws NotFoundException {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("/test-no-auth")
    public String testNoAuth() {
        return "OK-no-auth";
    }

    @PostMapping("/test-with-auth")
    @PreAuthorize("hasAuthority('ADMIN')") // метод сможет вызвать только пользователь с правами ADMIN
    public String testWithAuth() {
        return "OK-with-auth";
    }

    // активация
    @PostMapping("/account-activate")
    public ResponseEntity<Boolean> activate(@NotNull @RequestBody String uuid) throws UserActivateException {
        return ResponseEntity.ok(userService.activate(true, uuid) == 1);
    }

    // деактивация
    @PostMapping("/account-deactivate")
    public ResponseEntity<Boolean> deactivate(@NotNull @RequestBody String uuid) throws UserActivateException {
        return ResponseEntity.ok(userService.activate(false, uuid) == 1);
    }

    // регистрация
    @PutMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) throws UserExistException, RoleExistException {
        Activity activity = new Activity();
        activity.setUser(user);

        // получение уникольного UUID для активации пользователя
        activity.setUuid(UUID.randomUUID().toString());
        userService.register(user, activity);

        // в параллельном потоке отправляется email для подтверждения регистрации
        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activity.getUuid());;
        return ResponseEntity.ok().build(); //http OK - 200, регистрация прошла успешно
    }

    // авторизация (метод доступен для всех пользователей)
    // https://medium.com/geekculture/spring-security-authentication-process-authentication-flow-behind-the-scenes-d56da63f04fa
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) {

        // получение данных пользователя после успешной аутентификации
        UserDetailsImpl userDetails = userService.login(user);

        // проверка на активацию пользователя
        if (!userDetails.getUser().getActivity().isActivated()) {
            throw new DisabledException("User not activate");
        }

        /*
         после каждого успешного входа генерируется новый jwt,
         чтобы следующие запросы на backend авторизовывать автоматически
        */
        String jwt = jwtUtils.createAccessToken(userDetails.getUser());
        /*
         создание кука cо значением JWT для аутентификации на сервере
         (клиент будет отправлять его автоматически на backend при каждом запросе)
         */
        HttpCookie httpCookie = cookieUtils.createJwtCookie(jwt);

        // добавление кука в заголовок ответа
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, httpCookie.toString());

        // отправление клиенту данных пользователя + jwt-кук в заголовке Set-Cookie
        return ResponseEntity.ok().headers(responseHeaders).body(userDetails.getUser());
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyAuthority('USER')") // метод сможет вызвать только пользователь с правами USER
    public ResponseEntity<User> logout() {

        // создание кук с истекшим сроком действия. Автоматически удалится браузером т.к. срок действия = 0
        HttpCookie cookie = cookieUtils.deleteCookie();

        // добавление кука в заголовок ответа
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());

        // в ответе отправляем только кук, без тела запроса
        return ResponseEntity.ok().headers(httpHeaders).build();
    }

    // повторная отправка письма для активации аккаунта
    @PostMapping("/resend-activate-email")
    public ResponseEntity resendActivateEmail(@Nullable @RequestBody String username) throws UserActivateException {

        // поиск мб как по username так и email
        UserDetailsImpl user = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
        Activity activity = userService.findActivityByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Activity not found with user: " + username));

        if (activity.isActivated()) {
            throw new UserActivateException("User already activated: " + username);
        }

        // отправка письма активации (выполняется в параллельном потоке с помощью @Async, чтобы пользователь не ждал)
        emailService.sendActivationEmail(user.getEmail(), user.getUsername(), activity.getUuid());
        return ResponseEntity.ok().build(); // во всех случаях просто возвращается статус 200 - ОК
    }

    // отправка письма для сброса пароля
    @PostMapping("/send-reset-password-email")
    public ResponseEntity sendEmailResetPassword(@Nullable @RequestBody String email) {
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
        User user = userDetails.getUser(); // получаем текущего пользователя из контейнера UserDetails
        if (userDetails != null) {
            // отправление письма со ссылкой для сброса пароля (выполняется в параллельном потоке)
            emailService.sendResetPasswordEmail(user.getEmail(), jwtUtils.createResetPasswordToken(user));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update-password")
    @PreAuthorize("hasAnyAuthority('USER')")
    public ResponseEntity<Boolean> updatePassword(@Nullable @RequestBody String password) {
        // кол-во обновленных записей (в нашем случае должно быть 1, т.к. обновляем пароль одного пользователя)
        boolean isUpdatedCountMoreOne = userService.updatePassword(password);
        if(!isUpdatedCountMoreOne) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(isUpdatedCountMoreOne);
    }

    /*
     * Передача ошибки клиенту в формате Json
     * AuthenticationException.class - обработка только ошибок, связанных с аутентификацией
     * Exception - всех ошибок
     */
    @ExceptionHandler(Exception.class) //@ExceptionHandler позволяет перехватывать ошибки
    public ResponseEntity<JsonException> handleException(Exception ex) {

        /**
         DisabledException - не активирован
         UserAlreadyActivatedException - пользователь уже активирован (пытается неск. раз активировать)
         UsernameNotFoundException - username или email не найден в базе

         BadCredentialsException - неверные данные пользователя
         UserOrEmailExistsException - пользователь или email уже существуют
         DataIntegrityViolationException - ошибка уникальности в БД

         Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)
         */
        return new ResponseEntity<>(new JsonException(
                ex.getClass().getSimpleName(), // передача типа ошибки
                ex.getMessage()), // передача текста ошибки
                HttpStatus.BAD_REQUEST);
    }
}
