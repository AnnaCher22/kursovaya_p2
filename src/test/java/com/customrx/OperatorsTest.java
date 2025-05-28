package com.customrx;

import com.customrx.core.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OperatorsTest {

    @Test
    void testMapOperator() {
        List<Integer> results = new ArrayList<>();
        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onComplete();
                })
                .map(x -> x * 3)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable error) {}

                    @Override
                    public void onComplete() {}
                });

        assertEquals(Arrays.asList(3, 6), results);
    }

    @Test
    void testFilterOperator() {
        List<String> results = new ArrayList<>();
        Observable.create((Emitter<String> emitter) -> {
                    emitter.onNext("apple");
                    emitter.onNext("banana");
                    emitter.onNext("orange");
                    emitter.onComplete();
                })
                .filter(s -> s.length() > 5)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable error) {}

                    @Override
                    public void onComplete() {}
                });

        assertEquals(Arrays.asList("banana", "orange"), results);
    }

    @Test
    void testMapAndFilterCombined() {
        List<Integer> results = new ArrayList<>();
        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(10);
                    emitter.onNext(20);
                    emitter.onNext(30);
                    emitter.onComplete();
                })
                .map(x -> x + 5)
                .filter(x -> x > 20)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable error) {}

                    @Override
                    public void onComplete() {}
                });

        assertEquals(Arrays.asList(25, 35), results);
    }

    @Test
    void testFlatMapOperator() {
        List<String> results = new ArrayList<>();

        // Явное указание типов
        Observable.<String>create((Emitter<String> emitter) -> {
                    emitter.onNext("Hello");
                    emitter.onNext("World");
                    emitter.onComplete();
                })
                .flatMap(s -> {
                    // Явное создание Observable<String>
                    return Observable.<String>create(emitter -> {
                        for (char c : s.toCharArray()) {
                            emitter.onNext(String.valueOf(c));
                        }
                        emitter.onComplete();
                    });
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable error) {}

                    @Override
                    public void onComplete() {}
                });

        List<String> expected = Arrays.asList("H", "e", "l", "l", "o", "W", "o", "r", "l", "d");
        assertEquals(expected, results);
    }
}