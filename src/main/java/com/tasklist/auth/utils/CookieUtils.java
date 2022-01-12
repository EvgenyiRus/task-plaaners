package com.tasklist.auth.utils;

import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Утилита для работы с куками
 * кук jwt создается на сервере и управляется только сервером (создается, удаляется) - "server-side cookie"
 * На клиенте этот кук нельзя считать с помощью JavaScript (т.к. стоит флаг httpOnly) - для безопасности и защиты от XSS атак.
 * Также, обязательно канал должен быть HTTPS, чтобы нельзя было дешифровать данные запросов между клиентом и сервером
 */

@Component
public class CookieUtils {
    private static final String ACCESS_TOKEN = "access_token"; // имя кука для хранения JWT

    @Value("${jwt.cookie-max-age}")
    private int cookieAccessTokenDuration;

    @Value("${server.domain}")
    private String cookieAccessTokenDomain;

    // создание кука на сервере для аутентификации клиента
    public HttpCookie createJwtCookie(String jwt) {
        return ResponseCookie
                .from(ACCESS_TOKEN, jwt) // имя и значение для кука
                .maxAge(cookieAccessTokenDuration) // время действия

                /*
                   Запрет на отправку сервером кука (значение STRICT), если запрос пришел от стороннего сайта (защита от CSRF атак).
                   кук отправится клиенту, только если клиент сам введет в URL нужный адрес сервера и отправит запрос
                 */
                .sameSite(SameSiteCookies.STRICT.getValue())

                /*
                   Кук будет доступен для считывания только на сервере (на клиенте НЕ будет доступен с помощью JavaScript,
                   защита от XSS атак)
                 */
                .httpOnly(true)
                .secure(true) // кук будет передаваться браузером на backend только если канал будет защищен (https)

                /*
                   Перед отправкой запроса на сервер, браузер клиента определит на какой домен отпр-я запрос.
                   если значение запроса совпадает с cookieAccessTokenDomain, тогда браузер клиента прикрепляет кук с access_token к запросу на сервер
                   На все другие домены кук отправляться не будет
                 */
                .domain(cookieAccessTokenDomain)
                .path("/") // кук будет доступен для всех URL сервера
                .build();
    }

    // получение значения куки access_token (JWT) из запроса
    public String getCookieAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String cook = null;
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (ACCESS_TOKEN.equals(cookie.getName())) { // поиск необходимого куки по названию
                cook = cookie.getValue(); // получить значение JWT
            }
        }
        return cook;
    }
}
