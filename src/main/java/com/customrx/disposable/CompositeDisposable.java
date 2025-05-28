package com.customrx.disposable;

import java.util.concurrent.CopyOnWriteArrayList;

public class CompositeDisposable implements Disposable {
    private final CopyOnWriteArrayList<Disposable> disposables = new CopyOnWriteArrayList<>();
    private volatile boolean disposed = false;

    public void add(Disposable disposable) {
        if (disposed) {
            disposable.dispose();
        } else {
            disposables.add(disposable);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        disposables.forEach(Disposable::dispose);
        disposables.clear();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}