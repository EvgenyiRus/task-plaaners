package com.tasklist.auth.service;

import com.tasklist.auth.entity.Activity;
import com.tasklist.auth.entity.Role;
import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.RoleExistException;
import com.tasklist.auth.exception.UserActivateException;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.repository.ActivityRepository;
import com.tasklist.auth.repository.RoleRepository;
import com.tasklist.auth.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    public static final String DEFAULT_USER = "USER";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ActivityRepository activityRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, ActivityRepository activityRepository,
                       RoleRepository roleRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(Long id) throws NotFoundException {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException(String.format("User not found with id = %s", id)));
    }

    // регистрация
    public void register(User user) throws AuthenticationException {
        // проверка на существование пользователя с необходимым логином или email
        if (isUserExistByUserEmail(user.getEmail())) {
            throw new UserExistException(String.format("User with email - %s already exist", user.getEmail()));
        } else if (isUserExistByUsername(user.getUsername())) {
            throw new UserExistException(String.format("User with login - %s already exist", user.getUsername()));
        }
        // добавление роли
        Role role = findByName(DEFAULT_USER).orElseThrow(
                () -> new RoleExistException("Not found default role for user"));
        // роль пользователя автоматически сохранится в user_role
        user.getRoles().add(role);
        // шифрование пароля. запись хеша пароля c помощью алгоритма BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        // активация пользователя
        Activity activity = getActivity(user);
        activityRepository.save(activity);
    }

    // активация
    // true конвертируется в 1 (см. Аctivity - @Type(type = "org.hibernate.type.NumericBooleanType")), false - 0
    public int activate(boolean activate, String uuid) throws UserActivateException {
        Activity activity = activityRepository.findByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("Activity not found with uuid:" + uuid));
        // проверка на активацию пользователя
        if (activity.isActivated() && activate) {
            throw new UserActivateException("User already activated");
        }
        return activityRepository.setActivity(activate, uuid);
    }

    public Optional<Activity> findActivityByUuid (String uuid) {
        return activityRepository.findByUuid(uuid);
    }

    private Activity getActivity(User user) {
        Activity activity = new Activity();
        activity.setUser(user);
        // получение уникольного UUID для активации пользователя
        activity.setUuid(UUID.randomUUID().toString());
        return activity;
    }

    //Проверка на существующего пользователя по Login
    private boolean isUserExistByUsername(String username) {
        return userRepository.getCountByUsername(username) > 0;
    }

    //... по email
    private boolean isUserExistByUserEmail(String email) {
        return userRepository.getCountByUserEmail(email) > 0;
    }

    private Optional<Role> findByName(String role) {
        return roleRepository.findByName(role);
    }
}
