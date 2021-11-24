package com.tasklist.development.service;

import com.tasklist.development.entity.Category;
import com.tasklist.development.repository.CategoryRepository;
import com.tasklist.development.search.CategorySearchValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
//@RequiredArgsConstructor
/*  Все методы должны выполниться без ошибок, чтобы транзакция завершилась
    Если возникнет исключение, то все выполненные операции откатятся (Rollback) */
@Transactional
public class CategoryService {

    CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findByUserEmail(String email) {
        return categoryRepository.findByUserEmailOrderByTitleAsc(email);
    }

    public Category addOrUpdate(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<Category> find(CategorySearchValues values) {
        return categoryRepository.find(values.getTitle(), values.getEmail());
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).get();
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
