package com.tasklist.auth.service;

import com.tasklist.auth.entity.User;
import com.tasklist.auth.exception.UserExistException;
import com.tasklist.auth.repository.UserRepository;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUser(Long id) throws NotFoundException {
        return userRepository.findByIdOrderByUsername(id).orElseThrow(
                () -> new NotFoundException(String.format("User not found with id = %s", id)));
    }

    public void saveOrUpdate(User user) throws UserExistException {
        //проверка на существование пользователя с необходимым логином или email
        if(isUserExistByUserEmail(user.getEmail())) {
            throw new UserExistException(String.format("User with email - %s already exist", user.getEmail()));
        }
        else if (isUserExistByUsername(user.getUsername())) {
            throw new UserExistException(String.format("User with login - %s already exist", user.getUsername()));
        }
        // шифрование пароля. запись хеша пароля c помощью алгоритма BCrypt
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public boolean isUserExistByUsername(String username) {
        return userRepository.getCountByUsername(username) > 0;
    }

    // -//- email
    public boolean isUserExistByUserEmail(String email) {
        return userRepository.getCountByUserEmail(email) > 0;
    }
}
