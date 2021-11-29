package com.tasklist.development.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskSearchValues {

    //Поля для поиска
    private String title;
    private Short completed;
    private Long priorityId;
    private Long categoryId;
    private Date dateFrom;
    private Date dateTo;
    private String email;

    //Постраничная настройка страниц
    private Integer pageNumber = 0;
    private Integer pageSize = 10; //кол. элементов на странице

    //Настройка сортировки
    private String sortColumn = "id"; //По умолчанию сортируем по id
    private String sortDirection; //Направление
}
