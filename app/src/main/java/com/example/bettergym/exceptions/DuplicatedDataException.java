package com.example.bettergym.exceptions;

public class DuplicatedDataException extends RuntimeException {
    public DuplicatedDataException(String s) {
        super(s);
    }
    public String what(){
        return super.getMessage();
    }
}
