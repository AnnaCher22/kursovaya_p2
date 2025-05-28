package com.customrx.operators;

import com.customrx.core.Observable;
import com.customrx.core.Observer;
import com.customrx.core.Emitter;
import com.customrx.core.ObservableOnSubscribe;
import java.util.function.Predicate;

public class FilterOperator<T> implements ObservableOnSubscribe<T> {
    private final Observable<T> source;
    private final Predicate<? super T> predicate;

    public FilterOperator(Observable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public void subscribe(Emitter<T> emitter) {
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item) && !emitter.isDisposed()) {
                        emitter.onNext(item);
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
