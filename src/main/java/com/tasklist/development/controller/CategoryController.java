package com.tasklist.development.controller;

import com.tasklist.development.entity.Category;
import com.tasklist.development.search.CategorySearchValues;
import com.tasklist.development.service.CategoryService;
import com.tasklist.development.util.MyLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/* @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON*/
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Использовать post для получения данных т.к.
    // получаем личные данные пользователя ( категории), которые не стоит передавать небезопасным get
    //+ email пользователя будет передаваться в теле метода, а не в открытом виде
    @PostMapping("/all")
    public ResponseEntity<List<Category>> getAll(@RequestBody String email) {
        log.info("Call CategoryController: get all categories================================");
        MyLogger.printMessage("Print my logger message to call CategoryController getAll categories method");
        return ResponseEntity.ok(categoryService.findByUserEmail(email));
    }

    @PutMapping
    public ResponseEntity<Category> add(@RequestBody Category category) {
        log.info("Call CategoryController: add new category ===============================");
        //Проверка на новый объект
        if (category.getId() != null && category.getId() != 0) {
            //406 Not Acceptable означает, что сервер не может вернуть ответ, соответствующий списку допустимых значений
            return new ResponseEntity("Id must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (category.getTitle() == null || category.getTitle().isBlank()) {
            return new ResponseEntity("Title not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        //получаем созданный в БД объект с новым id и отдаем клиенту
        return ResponseEntity.ok(categoryService.addOrUpdate(category));
    }

    @PatchMapping //Т.к. обновляем не весь объект а лишь его часть
    public ResponseEntity update(@RequestBody Category category) {
        log.info("Call CategoryController: update category ===============================");
        if (category.getId() == null || category.getId() == 0) {
            return new ResponseEntity("Id not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        if (category.getTitle() == null || category.getTitle().isBlank()) {
            return new ResponseEntity("Title not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        categoryService.addOrUpdate(category);
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestBody Long id) {
        log.info("Call CategoryController: delete category ===============================");
        if (id == null || id == 0) {
            return new ResponseEntity("Id not must be null", HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            categoryService.delete(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK); //Операция прошла успешно
    }

    @PostMapping("/search")
    public ResponseEntity<List<Category>> search(@RequestBody CategorySearchValues values) {
        log.info("Call CategoryController: search category ===============================");
        return ResponseEntity.ok(categoryService.find(values));
    }

    @PostMapping("/id")
    public ResponseEntity<Category> searchById(@RequestBody Long id) {
        log.info("Call CategoryController: search category by id===============================");
        Category category;
        try {
            category = categoryService.findById(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return new ResponseEntity("Category by id " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return ResponseEntity.ok(category);
    }
}
