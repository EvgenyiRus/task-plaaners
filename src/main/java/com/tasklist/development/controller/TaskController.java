package com.tasklist.development.controller;

import com.tasklist.development.entity.Task;
import com.tasklist.development.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {

    TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Использовать post для получения данных т.к.
    // получаем личные данные пользователя ( категории), которые не стоит передавать небезопасным get
    //+ email пользователя будет передаваться в теле метода, а не в открытом виде
    @PostMapping("/all")
    public ResponseEntity getAll(@RequestBody String email) {
        log.info("Call TaskController: get all task for user================================");
        if (email == null || email.isBlank()) {
            return new ResponseEntity("Title not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(taskService.findByUser(email));
    }

    @PutMapping
    public ResponseEntity add(@RequestBody Task task) {
        log.info("Call TaskController: add new task ===============================");
        //Проверка на новый объект
        //406 Not Acceptable означает, что сервер не может вернуть ответ, соответствующий списку допустимых значений
        if (task.getId() != null && task.getId() != 0) {
            return new ResponseEntity("Id must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            return new ResponseEntity("Title not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        //получаем созданный в БД объект с новым id и отдаем клиенту
        return ResponseEntity.ok(taskService.addOrUpdate(task));
    }

    @PatchMapping //Т.к. обновляем не весь объект а лишь его часть
    public ResponseEntity update(@RequestBody Task task) {
        log.info("Call TaskController: update task ===============================");
        if (task.getId() == null || task.getId() == 0) {
            return new ResponseEntity("Id not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            return new ResponseEntity("Title not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        taskService.addOrUpdate(task);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestBody Long id) {
        log.info("Call TaskController: delete task ===============================");
        if (id == null || id == 0) {
            return new ResponseEntity("Id not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            taskService.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK); //Операция прошла успешно
    }

    @PostMapping("/id")
    public ResponseEntity searchById(@RequestBody Long id) {
        log.info("Call TaskController: search task by id===============================");
        Task task;
        try {
            task = taskService.find(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("Category by id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(task);
    }
}
