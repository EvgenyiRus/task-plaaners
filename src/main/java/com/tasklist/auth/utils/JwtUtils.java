package com.tasklist.auth.utils;

import com.tasklist.auth.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/* Утилита для работы с токеном JWT (генерация, парсинг данных, валидация)
Сам jwt не шифруется т.к. он будет передаваться по HTTPS и автоматически будет шифроваться (нет смысла 2 раза шифровать)
*/

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret; // секретный ключ для создания JWT

    @Value("${jwt.access_token-expiration}")
    private int accessTokenExpiration; // время действия JWT

    // генерация JWT по данным пользователя
    public String createJwtToken(User user) {
        Date currentDate = new Date();

        return Jwts.builder()
                // добавление данных (claims - поля)
                .setSubject(user.getId().toString()) // subject - стандартное поле JWT
                .setIssuedAt(currentDate) // время начало действия JWT
                .setExpiration(new Date(currentDate.getTime() + accessTokenExpiration)) // продолжительность действия JWT
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // подписание данных секретным ключом. Кодировка массива байтов в base64.
                .compact(); // преобразование массива байт в одну строку для читабельности
    }
}
