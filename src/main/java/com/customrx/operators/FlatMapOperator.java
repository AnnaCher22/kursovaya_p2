package com.customrx.operators;

import com.customrx.core.Observable;
import com.customrx.core.Observer;
import com.customrx.core.Emitter;
import com.customrx.core.ObservableOnSubscribe;
import com.customrx.disposable.Disposable;
import com.customrx.disposable.CompositeDisposable;
import java.util.function.Function;

public class FlatMapOperator<T, R> implements ObservableOnSubscribe<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends Observable<R>> mapper;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public FlatMapOperator(Observable<T> source, Function<? super T, ? extends Observable<R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Emitter<R> emitter) {
        compositeDisposable.add(new Disposable() {
            private volatile boolean disposed = false;

            @Override
            public void dispose() {
                disposed = true;
                compositeDisposable.dispose();
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        });

        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (emitter.isDisposed()) return;

                    Observable<R> innerObservable = mapper.apply(item);
                    Disposable disposable = innerObservable.subscribe(
                            new Observer<R>() {
                                @Override
                                public void onNext(R value) {
                                    if (!emitter.isDisposed()) {
                                        emitter.onNext(value);
                                    }
                                }

                                @Override
                                public void onError(Throwable error) {
                                    emitter.onError(error);
                                }

                                @Override
                                public void onComplete() {
                                    // Ничего не делаем, ждем завершения всех потоков
                                }
                            }
                    );
                    compositeDisposable.add(disposable);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                emitter.onError(error);
            }

            @Override
            public void onComplete() {
                // Ждем завершения всех внутренних observable
                compositeDisposable.dispose();
                emitter.onComplete();
            }
        });
    }
}