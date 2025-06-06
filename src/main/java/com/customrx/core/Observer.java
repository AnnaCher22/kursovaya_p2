package com.customrx.core;

public interface Observer<T> {
    void onNext(T item);
    void onError(Throwable error);
    void onComplete();
}