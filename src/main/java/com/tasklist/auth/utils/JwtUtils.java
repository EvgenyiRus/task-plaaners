package com.tasklist.auth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasklist.auth.entity.User;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Утилита для работы с токеном JWT (генерация, парсинг данных, валидация)
 * Сам jwt не шифруется т.к. он будет передаваться по HTTPS и автоматически будет шифроваться (нет смысла 2 раза шифровать)
 */
@Component
@Slf4j
public class JwtUtils {
    private static final String CLAIM_USER_KEY = "user";

    @Value("${jwt.secret}")
    private String jwtSecret; // секретный ключ для создания JWT

    @Value("${jwt.access_token-expiration}")
    private int accessTokenExpiration; // время действия JWT

    // генерация JWT по данным пользователя
    public String createJwtToken(User user) {
        Date currentDate = new Date();

        // добавление данных пользователя(claims) в JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_KEY, user);
        claims.put(Claims.SUBJECT, user.getId()); // subject - стандартное поле JWT
        return Jwts.builder()
                .setClaims(claims) // добавление данных (claims - поля, записываемые в payload в токене)
                .setIssuedAt(currentDate) // время начало действия JWT
                .setExpiration(new Date(currentDate.getTime() + accessTokenExpiration)) // продолжительность действия JWT
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // подписание данных секретным ключом. Кодировка массива байтов в base64.
                .compact(); // преобразование массива байт в одну строку для читабельности
    }

    // проверить целостность данных (не истек ли срок jwt и пр.)
    public boolean validate(String jwt) {

        /*
        Сервер проверяет своим ключом JWT.
        Если подпись не прошла проверку (failed) - значит эти данные были подписаны не нашим secret ключом
        (или сами данные после подписи были изменены), а значит к данным нет доверия.
        Сервер может доверять только тем данным, которые подписаны его secret ключом.
        Этот ключ хранится только на сервере, а значит никто кроме сервера не мог им воспользоваться и подписать данные.
        */
        if (jwt == null || jwt.isBlank()) {
            return false;
        }
        try {
            Jwts.parser() // проверка формата на корректность
                    .setSigningKey(jwtSecret) // указать ключ для провеки
                    .isSigned(jwt); // проверка подписи
            return true;
        } catch (MalformedJwtException e) { // неверный формат
            log.error(String.format("Invalid JWT token: %s", jwt));
        } catch (ExpiredJwtException e) { // срок годности
            log.error(String.format("JWT token is expired: %s", jwt));
        } catch (UnsupportedJwtException e) { // неподдерживаемая версия
            log.error(String.format("JWT token is unsupported: %s", jwt));
        } catch (IllegalArgumentException e) { // неправильный аргумент
            log.error(String.format("JWT claims string is empty: %s", jwt));
        }
        return false;
    }

    // получение полей из JWT (данных пользователя)
    public User getClaims(String jwt) {
        Map map = (Map) Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt).getBody().get(CLAIM_USER_KEY);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(map, User.class);
    }
}
