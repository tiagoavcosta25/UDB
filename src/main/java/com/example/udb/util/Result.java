package com.example.udb.util;

public class Result<T> implements IValue {
    private final T data;
    private final String[] context;

    public Result(T data, String[] context) {
        this.data = data;
        this.context = context;
    }

    public static <T> Result<T> of(T data, String... contexts) {
        return new Result<T>(data, contexts);
    }

    public T getData() {
        return data;
    }

    public String[] getContext() {
        return context;
    }
}
