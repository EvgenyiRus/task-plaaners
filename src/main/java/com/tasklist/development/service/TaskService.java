package com.tasklist.development.service;

import com.tasklist.development.entity.Task;
import com.tasklist.development.repository.TaskRepository;
import com.tasklist.development.search.TaskSearchValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    public Page<Task> find (TaskSearchValues taskSearchValues) {

        String title = taskSearchValues.getTitle();
        String email = taskSearchValues.getEmail();
        Long priorityId = taskSearchValues.getPriorityId();
        Long categoryId = taskSearchValues.getCategoryId();
        Date dateFrom = taskSearchValues.getDateFrom();
        Date dateTo = taskSearchValues.getDateTo();
        Short completed = taskSearchValues.getCompleted();
        String sortDirection = taskSearchValues.getSortDirection();
        String sortColumn = taskSearchValues.getSortColumn();
        Integer pageSize = taskSearchValues.getPageSize();
        Integer pageNumber = taskSearchValues.getPageNumber();

        Sort.Direction direction = sortDirection == null || sortDirection.trim().length() == 0 || sortDirection.equals("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        // id - второй столбец для сортировки, если будет 2 задачи например с одинаковым приоритетом(и мы сортируем по приоритету)
        // Полей для сортировки мб сколько угодно
        Sort sort = Sort.by(direction, sortColumn, "id");

        // Объект "постраничности"
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        return taskRepository.find(title, completed, priorityId, categoryId, dateFrom, dateTo, email, pageRequest);
    }
}
