package com.tasklist.development.service;

import com.tasklist.development.entity.Task;
import com.tasklist.development.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
/*  Все методы должны выполниться без ошибок, чтобы транзакция завершилась
    Если возникнет исключение, то все выполненные операции откатятся (Rollback) */
@Transactional
public class TaskService {

    TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> findByUser (String email) {
        return taskRepository.findByUserEmailOrderByTitle(email);
    }

    public Task find (Long id) {
        return taskRepository.findById(id).get();
    }

    public Task addOrUpdate(Task task) {
        return taskRepository.save(task);
    }

    public void delete (Long id) {
        taskRepository.deleteById(id);
    }

    public Page<Task> find (String title, Short completed, Long priorityId,
                            Long categoryId, Date dateFrom, Date dateTo, String email, PageRequest request) {
        return taskRepository.find(title, completed, priorityId, categoryId, dateFrom, dateTo, email, request);
    }
}
