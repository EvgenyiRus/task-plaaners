package com.tasklist.development.repository;

import com.tasklist.development.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {

    Optional<Priority> findById (Long id);

    List<Priority> findByUserEmailOrderByIdAsc(String email);

    @Query("SELECT p FROM Priority p where " +
            "(:title is null or :title='' " + // если передадим параметр title пустым, то выберутся все записи (пустые и непустые)
            " or lower(p.title) like lower(concat('%', :title,'%'))) " + // если параметр title не пустой, то выберутся только совпадающие записи
            " and p.user.email=:email  " + // фильтрация для конкретного пользователя
            "order by p.title asc") // сортировка по названию
    List<Priority> find(@Param("title") String title, @Param("email") String email);
}
