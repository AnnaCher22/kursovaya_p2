package com.customrx.core;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}