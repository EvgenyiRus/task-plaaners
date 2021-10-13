package com.tasklist.development.util;

import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

@UtilityClass
@Log
public class MyLogger {
    public static void printMessage(String message) {
        System.out.println("=====================================================");
        log.info(message);
        System.out.println("=====================================================");
    }
}
