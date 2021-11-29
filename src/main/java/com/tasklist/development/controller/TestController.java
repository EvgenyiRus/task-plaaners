package com.tasklist.development.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class TestController {

    @RequestMapping("test")
    private String test(){
        return "Service is work";
    }
}
