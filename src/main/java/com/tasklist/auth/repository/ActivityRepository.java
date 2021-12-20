package com.tasklist.auth.repository;

import com.tasklist.auth.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // активация, деактивация пользователя
    @Modifying
    @Transactional
    @Query("UPDATE Activity a SET a.activated=:activate WHERE a.uuid=:uuid")
    int setActivity (@Param("activate") boolean activate, @Param("uuid") String uuid); // кол-о обновленных строк

    Optional<Activity> findByUserId(long id);

    Optional<Activity> findByUuid(String uuid);
}
