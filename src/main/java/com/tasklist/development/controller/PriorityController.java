package com.tasklist.development.controller;

import com.tasklist.development.entity.Priority;
import com.tasklist.development.search.PrioritySearchValues;
import com.tasklist.development.service.PriorityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/priority")
@Slf4j
public class PriorityController {

    PriorityService priorityService;

    @Autowired
    PriorityController(PriorityService priorityService) {
        this.priorityService = priorityService;
    }

    @PostMapping("/all")
    List<Priority> getAll(@RequestBody String email) {
        log.info("Call PriorityController: get all priorities================================");
        return priorityService.findByUserEmail(email);
    }

    @PutMapping
    ResponseEntity add(@RequestBody Priority priority) {
        log.info("Call PriorityController: add priority================================");
        if (priority.getId() != null && priority.getId() != 0) {
            return new ResponseEntity("Id must be empty", HttpStatus.NOT_ACCEPTABLE);
        }
        if (priority.getColor() == null || priority.getColor().isBlank()) {
            return new ResponseEntity("Color is empty or null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (priority.getTitle() == null || priority.getTitle().isBlank()) {
            return new ResponseEntity("Title is empty or null", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(priorityService.addOrUpdate(priority));
    }

    @PatchMapping
    ResponseEntity update(@RequestBody Priority priority) {
        log.info("Call PriorityController: update priority================================");
        if (priority.getId() == null || priority.getId() == 0) {
            return new ResponseEntity("Id is empty", HttpStatus.NOT_ACCEPTABLE);
        }
        if (priority.getColor() == null || priority.getColor().isBlank()) {
            return new ResponseEntity("Color is empty or null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (priority.getTitle() == null || priority.getTitle().isBlank()) {
            return new ResponseEntity("Title is empty or null", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(priorityService.addOrUpdate(priority));
    }

    @DeleteMapping
    ResponseEntity delete(@RequestBody Long id) {
        if (id == null || id == 0) {
            return new ResponseEntity("Id is empty", HttpStatus.NOT_ACCEPTABLE);
        }
        log.info("Call PriorityController: delete priority================================");
        try {
            priorityService.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @PostMapping("/search")
    public ResponseEntity search(@RequestBody PrioritySearchValues prioritySearchValues) {
        log.info("Call PriorityController: search priority ===============================");
        return ResponseEntity.ok(priorityService.find(prioritySearchValues));
    }

    @PostMapping("/id")
    public ResponseEntity searchById(@RequestBody Long id) {
        log.info("Call PriorityController: search priority by id ===============================");
        Priority priority;
        try {
            priority = priorityService.findById(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("Priority by id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(priority);
    }
}
