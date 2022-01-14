package com.tasklist.auth.repository;

import com.tasklist.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT COUNT(u.username) FROM User u WHERE lower(u.username)=lower(:username)")
    int getCountByUsername(@Param("username") String username);

    @Query("SELECT COUNT(u.email) FROM User u WHERE lower(u.email)=lower(:email)")
    int getCountByUserEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password=:password WHERE u.username=:username")
    int updatePasswordByUserName(@Param("password") String password, @Param("username") String username);
}
