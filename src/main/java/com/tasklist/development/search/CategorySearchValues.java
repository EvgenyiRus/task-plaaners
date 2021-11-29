package com.tasklist.development.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//Объект для поиска категорий по полям. Контейнер c JSON для получения и отправления клиенту
@Getter
@Setter
@AllArgsConstructor
public class CategorySearchValues extends SearchValues {
    //Можно добавлять любые свойства объектов по которым будет происходить поиск(не обязательно из Category)
}
