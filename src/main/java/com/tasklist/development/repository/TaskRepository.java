package com.tasklist.development.repository;

import com.tasklist.development.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    //Поиск задач пользователя
    List<Task> findByUserEmailOrderByTitle(String email);

    @Query("SELECT t FROM Task t WHERE " +
            "(:title is null or :title='' or lower(:title) like lower(concat('%',:title,'%'))) " +
            "and t.user.email=:email")
    Task find (@Param("title") String title,
               @Param("email") String email);
}
