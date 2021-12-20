package com.tasklist.auth.service;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/* Сервис, который используется для проверки пользователя в БД при аутентификации/авторизации (логин-пароль)
Метод loadUserByUsername автоматически вызывается Spring контейнером (когда пытаемся залогинить пользователя методом authenticate), чтобы найти пользователя в БД.
Затем Spring сравнивает хэши паролей (введенного и фактического) и выдает результат (все ок или выбрасывает исключение, которое можно оправить клиенту)
Также, метод loadUserByUsername можно вызывать самим, вручную, когда необходимо проверить наличие пользователя в БД (по username или email).
Чтобы этот класс был задействован в аутентификации - его нужно указать в Spring настройках в методе configure(AuthenticationManagerBuilder authenticationManagerBuilder)
Класс обязательно должен реализовать интерфейс UserDetailsService, чтобы Spring "принимал" этот класс.
 */
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    // поиск пользователя при аутентификации по username или email
    public UserDetails loadUserByUsername(String value) throws UsernameNotFoundException {
        Optional<User> authUser = userRepository.findByUsername(value); // поиск по username
        if (authUser.isEmpty()) {
            authUser = userRepository.findByEmail(value); // поиск по email
        }
        if (authUser.isEmpty()) {
            throw new UsernameNotFoundException(String.format("User %s not found", value));
        }
        return new UserDetailsImpl(authUser.get());
    }

    @Transactional
    // поиск пользователя при аутентификации по username или email
    public UserDetails loadUserByUsername(Long id) throws UsernameNotFoundException {
        Optional<User> authUser = userRepository.findById(id);
        if (authUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new UserDetailsImpl(authUser.get());
    }
}
