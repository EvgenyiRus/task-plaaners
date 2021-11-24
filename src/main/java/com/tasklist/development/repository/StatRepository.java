package com.tasklist.development.repository;

import com.tasklist.development.entity.Stat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatRepository extends CrudRepository<Stat, Long> { //Crud т.к. не нужны все методы из JPA
    Stat findByUserEmailOrderByUserEmailAsc(String email);
}
