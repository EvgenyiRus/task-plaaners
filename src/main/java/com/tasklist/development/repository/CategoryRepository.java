package com.tasklist.development.repository;

import com.tasklist.development.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // поиск категорий пользователя (по email)
    List<Category> findByUserEmailOrderByTitleAsc(String email);

    // поиск по значению для конкретноого пользователя (JPQL)
    @Query("SELECT c FROM Category c where " +
            "(:title is null or :title='' " + // если передадим параметр title пустым, то выберутся все записи (пустые и непустые)
            " or lower(c.title) like lower(concat('%', :title,'%'))) " + // если параметр title не пустой, то выберутся только совпадающие записи
            " and c.user.email=:email  " + // фильтрация для конкретного пользователя
            "order by c.title asc")
    // сортировка по названию
    List<Category> find(@Param("title") String title, @Param("email") String email);

    Optional<Category> findById(Long id);
}

