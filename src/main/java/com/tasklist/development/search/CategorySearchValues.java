package com.tasklist.development.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//Объект для поиска категорий по полям. Контейнер c JSON для получения и отправления клиенту
@Getter
@Setter
@NoArgsConstructor
public class CategorySearchValues extends SearchValues {
    //Можно добавлять любые свойства объектов по которым будет происходить поиск(не обязательно из Category)
}
