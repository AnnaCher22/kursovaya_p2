package com.customrx;

import com.customrx.core.Emitter;
import com.customrx.core.Observable;
import com.customrx.core.Observer;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testBasicObservable() {
        List<Integer> results = new ArrayList<>();
        Observable.create((Emitter<Integer> emitter) -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable error) {}

            @Override
            public void onComplete() {}
        });

        assertEquals(Arrays.asList(1, 2), results);
    }

    @Test
    void testMultipleSubscribers() {
        AtomicInteger counter1 = new AtomicInteger();
        AtomicInteger counter2 = new AtomicInteger();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                counter1.incrementAndGet();
            }

            @Override
            public void onError(Throwable error) {}

            @Override
            public void onComplete() {}
        });

        observable.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                counter2.incrementAndGet();
            }

            @Override
            public void onError(Throwable error) {}

            @Override
            public void onComplete() {}
        });

        assertEquals(2, counter1.get());
        assertEquals(2, counter2.get());
    }

    @Test
    void testOnComplete() {
        AtomicInteger completeCount = new AtomicInteger();
        Observable.create((Emitter<String> emitter) -> {
            emitter.onComplete();
        }).subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {}

            @Override
            public void onError(Throwable error) {}

            @Override
            public void onComplete() {
                completeCount.incrementAndGet();
            }
        });

        assertEquals(1, completeCount.get());
    }

    @Test
    void testEmptyObservable() {
        List<Integer> results = new ArrayList<>();
        Observable.create((Emitter<Integer> emitter) -> {
            emitter.onComplete();
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable error) {}

            @Override
            public void onComplete() {}
        });

        assertTrue(results.isEmpty());
    }
}