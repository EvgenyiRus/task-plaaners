package com.tasklist.auth.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//POJO класс для передачи информации в виде JSON
@Getter
@Setter
@AllArgsConstructor
public class JsonException {

    String exception; // Тип ошибки
    String message; // Текст ошибки
}
