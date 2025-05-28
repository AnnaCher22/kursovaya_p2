package com.customrx.operators;

import com.customrx.core.Observable;
import com.customrx.core.Observer;
import com.customrx.core.Emitter;
import com.customrx.core.ObservableOnSubscribe;
import java.util.function.Function;

public class MapOperator<T, R> implements ObservableOnSubscribe<R> {
    private final Observable<T> source;
    private final Function<? super T, ? extends R> mapper;

    public MapOperator(Observable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public void subscribe(Emitter<R> emitter) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(mapper.apply(item));
                    }
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
                emitter.onComplete();
            }
        });
    }
}