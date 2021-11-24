package com.tasklist.development.service;

import com.tasklist.development.entity.Priority;
import com.tasklist.development.repository.PriorityRepository;
import com.tasklist.development.search.PrioritySearchValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
/*  Все методы должны выполниться без ошибок, чтобы транзакция завершилась
    Если возникнет исключение, то все выполненные операции откатятся (Rollback) */
@Transactional
public class PriorityService {

    private final PriorityRepository priorityRepository;

    @Autowired
    public PriorityService(PriorityRepository priorityRepository) {
        this.priorityRepository = priorityRepository;
    }

    public List<Priority> findByUserEmail(String email) {
        return priorityRepository.findByUserEmailOrderByIdAsc(email);
    }

    public Priority addOrUpdate(Priority category) {
        return priorityRepository.save(category);
    }

    public void delete(Long id) {
        priorityRepository.deleteById(id);
    }

    public List<Priority> find(PrioritySearchValues values) {
        return priorityRepository.find(values.getTitle(), values.getEmail());
    }

    public Priority findById(Long id) {
        return priorityRepository.findById(id).get();
    }
}
