package com.tasklist.development.repository;

import com.tasklist.development.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    //Поиск задач пользователя
    List<Task> findByUserEmailOrderByTitle(String email);

    //Поиск по всем параметрам задачи(мб null - значит не учиывать при поиске)
    @Query("SELECT t FROM Task t WHERE " +
            "(:title is null or :title='' or lower(t.title) like lower(concat('%',:title,'%'))) and" +
            "(:completed is null or t.completed=:completed) and " +
            "(:priorityId is null or t.priority.id=:priorityId) and " +
            "(:categoryId is null or t.category.id=:categoryId) and " +
            "(cast(:dateFrom as timestamp) is null or cast(:dateTo as timestamp) is null or " +
            "t.taskDate between to_date(:dateFrom,'YYYY-MM-DD') and to_date(:dateTo,'YYYY-MM-DD')) and " +
            "(t.user.email=:email)")
    Page<Task> find (@Param("title") String title,
                     @Param("completed") Short completed,
                     @Param("priorityId") Long priorityId,
                     @Param("categoryId") Long categoryId,
                     @Param("dateFrom") Date dateFrom,
                     @Param("dateTo") Date dateTo,
                     @Param("email") String email,
                     Pageable page);
}
