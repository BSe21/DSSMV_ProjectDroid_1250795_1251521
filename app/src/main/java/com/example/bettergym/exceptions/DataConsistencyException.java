package com.example.bettergym.exceptions;

public class DataConsistencyException extends RuntimeException {
    public DataConsistencyException(String s) {
        super(s);
    }
    public String what(){
        return super.getMessage();
    }
}
