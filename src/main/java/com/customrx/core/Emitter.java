package com.customrx.core;

import com.customrx.disposable.Disposable;

public interface Emitter<T> extends Disposable {
    void onNext(T value);
    void onError(Throwable error);
    void onComplete();
}