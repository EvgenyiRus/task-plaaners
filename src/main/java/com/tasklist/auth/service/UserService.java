package com.tasklist.auth.service;

import com.tasklist.auth.entity.Role;
import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.RoleExistException;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.repository.RoleRepository;
import com.tasklist.auth.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    public static final String DEFAULT_USER = "USER";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUser(Long id) throws NotFoundException {
        return userRepository.findByIdOrderByUsername(id).orElseThrow(
                () -> new NotFoundException(String.format("User not found with id = %s", id)));
    }

    public void saveOrUpdate(User user) throws AuthenticationException {
        //проверка на существование пользователя с необходимым логином или email
        if (isUserExistByUserEmail(user.getEmail())) {
            throw new UserExistException(String.format("User with email - %s already exist", user.getEmail()));
        } else if (isUserExistByUsername(user.getUsername())) {
            throw new UserExistException(String.format("User with login - %s already exist", user.getUsername()));
        }
        // получение роли по умолчанию
        Role role = findByName(DEFAULT_USER).orElseThrow(
                () -> new RoleExistException("Not found default role for user"));
        // роль пользователя автоматически сохранится в user_role
        user.getRoles().add(role);
        // шифрование пароля. запись хеша пароля c помощью алгоритма BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    //Проверка на существующего пользователя по Login
    public boolean isUserExistByUsername(String username) {
        return userRepository.getCountByUsername(username) > 0;
    }

    //... по email
    public boolean isUserExistByUserEmail(String email) {
        return userRepository.getCountByUserEmail(email) > 0;
    }

    public Optional<Role> findByName(String role) {
        return roleRepository.findByName(role);
    }
}
