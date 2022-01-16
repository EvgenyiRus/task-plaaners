package com.tasklist.auth.service;

import com.tasklist.auth.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/** объект для хранения данных пользователя
 используется при аутентификации пользователя в Spring контейнере
 User implements UserDetails не рекомендуется
 */
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

    private User user;
    private Set<? extends GrantedAuthority> authorities; // все права пользователя - эту переменную использует Spring контейнер

    public UserDetailsImpl(User user) {
        this.user = user;

        // получение списка прав пользователя(роль мб 1 и иметь несколько прав)
        authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    // по умолчанию все методы = false
    // действующий пользователь
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // заблокированный
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // с действующим паролем
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /* Метод isEnabled вызывается автоматически Spring контейнером, где это нужно ему по логике работы =>
       => метод может вызываться до проверки "логин-пароль"
       Поэтому исключение DisabledException (пользователь деактивирован) выбросится до того, как будет проверено
       верно ли введены "логин-пароль"

       релизация метода: чтобы пользователь проверялся на активность только после успешного ввода логина-пароля

       поэтому в нужных местах кода сами будем проверять, активирован аккаунт или нет с помощью поля active из БД.

       если пользователь неактивен - выбрасываем исключение (готовый класс DisabledException).
       поэтому метод всегда возвращает true.
    */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
