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
        http.csrf().disable(); // отключаем на время разработки(для post put и др. которые изменяют данные)
        http.httpBasic().disable(); //отключаем стандартную авторизацию Spring
        http.formLogin().disable(); //отключаем стандартную форму логирования
        http.requiresChannel().anyRequest().requiresSecure(); //Обязательное исп. HTTPS
    }
}
