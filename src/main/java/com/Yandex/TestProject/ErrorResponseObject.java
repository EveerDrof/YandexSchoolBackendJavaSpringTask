package com.Yandex.TestProject;

import org.springframework.http.HttpStatus;

public class ErrorResponseObject {
    private int code;
    private String message;

    public ErrorResponseObject(HttpStatus code, String message) {

        this.code = code.value();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
