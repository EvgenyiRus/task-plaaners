package com.tasklist.auth.service;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/* сервис, который используется для проверки пользователя в БД при аутентификации/авторизации (логин-пароль)
    метод loadUserByUsername автоматически вызывается Spring контейнером (когда пытаемся залогинить пользователя методом authenticate), чтобы найти пользователя в БД.
    затем Spring сравнивает хэши паролей (введенного и фактического) и выдает результат (все ок или выбрасывает исключение, которое можно оправить клиенту)
    также, метод loadUserByUsername можно вызывать самим, вручную, когда необходимо проверить наличие пользователя в БД (по username или email).
    чтобы этот класс был задействован в аутентификации - его нужно указать в Spring настройках в методе configure(AuthenticationManagerBuilder authenticationManagerBuilder)
    класс обязательно должен реализовать интерфейс UserDetailsService, чтобы Spring "принимал" этот класс.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    // поиск пользователя при аутентификации по username или email
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> authUser = userRepository.findByUsername(username); // поиск по username
        if (authUser.isEmpty()) {
            authUser = userRepository.findByEmail(username); // поиск по email
        }
        if (authUser.isEmpty()) {
            throw new UsernameNotFoundException(String.format("User %s not found", username));
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
