package com.customrx.disposable;

public interface Disposable {
    void dispose();
    boolean isDisposed();
}