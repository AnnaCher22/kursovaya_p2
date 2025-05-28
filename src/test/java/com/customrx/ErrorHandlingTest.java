package com.customrx;

import com.customrx.core.*;
import com.customrx.disposable.Disposable;
import com.customrx.schedulers.IOScheduler;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingTest {

    @Test
    void testErrorPropagation() {
        AtomicReference<Throwable> receivedError = new AtomicReference<>();
        String errorMessage = "Test error";

        Observable.create((Emitter<String> emitter) -> {
            emitter.onError(new RuntimeException(errorMessage));
        }).subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
            }

            @Override
            public void onError(Throwable error) {
                receivedError.set(error);
            }

            @Override
            public void onComplete() {
            }
        });

        assertNotNull(receivedError.get());
        assertEquals(errorMessage, receivedError.get().getMessage());
    }

    @Test
    void testErrorInOperator() {
        List<Integer> results = new ArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onComplete();
                })
                .map(x -> {
                    if (x == 2) throw new RuntimeException("Invalid value");
                    return x;
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable e) {
                        error.set(e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertEquals(Arrays.asList(1), results);
        assertNotNull(error.get());
        assertEquals("Invalid value", error.get().getMessage());
    }

    @Test
    void testErrorStopsStream() {
        List<Integer> results = new ArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);

        Observable.create((Emitter<Integer> emitter) -> {
            emitter.onNext(1);
            emitter.onError(new RuntimeException("Error"));
            emitter.onNext(2); // Не должно быть доставлено
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable error) {
            }

            @Override
            public void onComplete() {
                completed.set(true);
            }
        });

        assertEquals(Arrays.asList(1), results);
        assertFalse(completed.get());
    }

    @Test
    void testDisposableOnError() {
        AtomicBoolean disposed = new AtomicBoolean(false);

        Observable.create((Emitter<String> emitter) -> {
            emitter.onError(new RuntimeException("Test"));
        }).subscribe(new Observer<String>() {
            @Override
            public void onNext(String item) {
            }

            @Override
            public void onError(Throwable error) {
                disposed.set(true);
            }

            @Override
            public void onComplete() {
            }
        });

        assertTrue(disposed.get());
    }

    @Test
    void testDisposable() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger lastValue = new AtomicInteger(-1);

        Disposable disposable = Observable.<Integer>create(emitter -> {
                    for (int i = 0; i < 10; i++) {
                        if (emitter.isDisposed()) {
                            System.out.println("Emitter disposed, stopping at: " + i);
                            break;
                        }

                        try {
                            Thread.sleep(100); // Обработка InterruptedException
                        } catch (InterruptedException e) {
                            // Если поток был прерван, завершаем работу
                            System.out.println("Thread interrupted, stopping emission");
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                            return;
                        }

                        if (emitter.isDisposed()) {
                            System.out.println("Emitter disposed after sleep, stopping at: " + i);
                            break;
                        }

                        emitter.onNext(i);
                    }

                    if (!emitter.isDisposed()) {
                        emitter.onComplete();
                    }
                })
                .subscribeOn(new IOScheduler())
                .subscribe(
                        item -> {
                            lastValue.set(item);
                            counter.incrementAndGet();
                            System.out.println("Received: " + item);
                        },
                        error -> latch.countDown(),
                        latch::countDown
                );

        // Даем время для обработки нескольких элементов
        Thread.sleep(350);
        disposable.dispose();
        System.out.println("Disposed at value: " + lastValue.get());

        // Даем время для завершения
        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

        // Проверяем что обработано от 1 до 4 элементов
        int finalCount = counter.get();
        System.out.println("Final count: " + finalCount);
        assertTrue(finalCount >= 1 && finalCount <= 4,
                "Counter should be between 1 and 4, but was " + finalCount);
    }
}