package com.customrx.operators;

import com.customrx.core.Observable;
import com.customrx.core.ObservableOnSubscribe;
import com.customrx.core.Emitter;
import com.customrx.core.Observer;
import com.customrx.schedulers.Scheduler;

public class SubscribeOnOperator<T> implements ObservableOnSubscribe<T> {
    private final Observable<T> source;
    private final Scheduler scheduler;

    public SubscribeOnOperator(Observable<T> source, Scheduler scheduler) {
        this.source = source;
        this.scheduler = scheduler;
    }

    @Override
    public void subscribe(Emitter<T> emitter) {
        scheduler.schedule(() ->
                source.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        emitter.onNext(item);
                    }

                    @Override
                    public void onError(Throwable error) {
                        emitter.onError(error);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                })
        );
    }
}