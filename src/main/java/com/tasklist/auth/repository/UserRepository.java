package com.tasklist.auth.repository;

import com.tasklist.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdOrderByUsername(Long id);

    @Query("SELECT COUNT(u.username) FROM User u WHERE lower(u.username)=lower(:username)")
    int getCountByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u.email) FROM User u WHERE lower(u.email)=lower(:email)")
    int getCountByUserEmail(@Param("email") String email);
}
