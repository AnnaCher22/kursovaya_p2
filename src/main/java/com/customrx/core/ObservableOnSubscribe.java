package com.customrx.core;

@FunctionalInterface
public interface ObservableOnSubscribe<T> {
    void subscribe(Emitter<T> emitter);
}