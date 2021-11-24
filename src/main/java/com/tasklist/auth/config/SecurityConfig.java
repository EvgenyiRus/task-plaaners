package com.tasklist.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity(debug = true) //для вывода подробностей в лог
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /* если используется другая клиентская технология (не SpringMVC, а например Angular, React и пр.),
            то выключаем встроенную Spring-защиту от CSRF атак,
            иначе запросы от клиента не будут обрабатываться, т.к. Spring Security будет пытаться в каждом входящем запроcе искать спец. токен для защиты от CSRF
        */
        http.csrf().disable(); // отключаем на время разработки(для методов post put и др.
                                // которые изменяют данные, будут без ошибок)
        http.httpBasic().disable(); //отключаем стандартную авторизацию Spring
        http.formLogin().disable(); //отключаем стандартную форму логирования
        http.requiresChannel().anyRequest().requiresSecure(); //Обязательное исп. HTTPS
    }
}
