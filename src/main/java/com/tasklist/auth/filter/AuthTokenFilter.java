package com.tasklist.auth.filter;

import com.tasklist.auth.exception.JwtCommonException;
import com.tasklist.auth.utils.CookieUtils;
import com.tasklist.auth.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
  Класс, который отвечает за весь функционал аутентификации/авторизации, перехватывает все запросы
  (login, logout, получение списка задач, редактирование и пр.)
  Примечание:
  Все входящие запросы сначала обрабатывает фильтр AuthTokenFilter: он проверяет URI, если необходимо - считывает jwt из кука.
  Если запрос пришел на публичную ссылку (авторизация, запрос на обновление пароля и пр.),
  то JWT не требуется и просто продолжается выполнение запроса.
  Если запрос пришел на закрытую ссылку (список задач, редактирование и пр. - это только для авторизованных пользователей)
  - сначала фильтр AuthTokenFilter должен получить JWT.
  После получения и валидации jwt фильтр AuthTokenFilter аутентифицирует пользователя и добавляет его в Spring контейнер
  (объект Authrorization).
  Только после этого - запрос передается дальше в контроллер для выполнения.
 */

@Component
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private CookieUtils cookieUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    public void setCookieUtils(CookieUtils cookieUtils) {
        this.cookieUtils = cookieUtils;
    }

    // вызывается автоматически при каждом запросе
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        // проверка запроса на наличие открытых API
        boolean isRequestToNotPublicAPI = getPermitUrls().stream().noneMatch(request ->
                httpServletRequest.getRequestURI().toLowerCase().contains(request));

        // если закрытй API и пользоветель еще не аутентифицирован
        if (isRequestToNotPublicAPI) {
            String jwt = cookieUtils.getCookieAccessToken(httpServletRequest);
            if (jwt == null) {
                throw new AuthenticationCredentialsNotFoundException("token not found");
            }

            // проверка на аутентификацию пользователя
            if (!jwtUtils.validate(jwt)) {
                throw new JwtCommonException("jwt validate exception");
            }

            // пользователь авторизован успешно
            log.info("jwt: " + jwt);

            /*
            Необходимо считать все данные пользователя из JWT, чтобы получить userDetails,
            добавить его в Spring контейнер (авторизовать) и не делать ни одного запроса в БД
            Запрос в БД выполняется только 1 раз, когда пользователь залогинился.
            После этого аутентификация/авторизация проходит автоматически с помощью JWT
            Создается объект userDetails на основе данных JWT (все поля, кроме пароля)
             */
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse); // проброс дальше в контроллер
    }

    // подготовка списка разрешенных URL (открытых API), которые не требуют проверки JWT
    private List<String> getPermitUrls() {
        return Arrays.asList(
                "register", // регистрация нового пользователя
                "login", // аутентификация (логин-пароль)
                "activate-account", // активация нового пользователя
                "resend-activate-email", // запрос о повторной отправки письма активации
                "send-reset-password-email", // запрос на отправку письма об обновлении пароля
                "test-no-auth", // тестовый URL для проверки работы backend
                "index"); // главная страница
    }
}
