package com.customrx.examples;

import com.customrx.core.*;
import com.customrx.schedulers.*;
import com.customrx.disposable.Disposable;

import java.util.concurrent.atomic.AtomicBoolean;

public class BasicExample {

    public static void main(String[] args) throws InterruptedException {
        // 1. Basic Chain
        System.out.println("=== Basic Chain Example ===");
        Disposable basicDisposable = Observable.<Integer>create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                    emitter.onComplete();
                })
                .map(x -> x * 2)
                .filter(x -> x > 3)
                .subscribe(
                        item -> System.out.println("[Basic] Received: " + item),
                        error -> System.err.println("[Basic] Error: " + error),
                        () -> System.out.println("[Basic] Completed")
                );

        // 2. Async Operations
        System.out.println("\n=== Async Example ===");
        Observable.<String>create(emitter -> {
                    System.out.println("[Async] Emitting on thread: " + Thread.currentThread().getName());
                    emitter.onNext("A");
                    emitter.onNext("B");
                    emitter.onComplete();
                })
                .subscribeOn(new IOScheduler())
                .observeOn(new ComputationScheduler())
                .map(s -> s + "-processed")
                .observeOn(new SingleThreadScheduler())
                .subscribe(
                        item -> System.out.println("[Async] Received '" + item + "' on thread: " + Thread.currentThread().getName()),
                        Throwable::printStackTrace,
                        () -> System.out.println("[Async] Completed on thread: " + Thread.currentThread().getName())
                );

        // 3. Disposable Example (полностью исправлено)
        System.out.println("\n=== Disposable Example ===");
        AtomicBoolean shouldStop = new AtomicBoolean(false);

        Disposable longRunning = Observable.<Integer>create(emitter -> {
                    for (int i = 0; i < 10; i++) {
                        // Двойная проверка с синхронизацией
                        synchronized (shouldStop) {
                            if (shouldStop.get() || emitter.isDisposed()) {
                                System.out.println("[Disposable] Stopping emission due to cancellation");
                                return;
                            }
                        }

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            System.out.println("[Disposable] Thread interrupted, stopping");
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                            return;
                        }

                        // Повторная проверка после сна
                        synchronized (shouldStop) {
                            if (shouldStop.get() || emitter.isDisposed()) {
                                System.out.println("[Disposable] Cancellation detected after sleep");
                                return;
                            }
                        }

                        System.out.println("[Disposable] Emitting: " + i);
                        emitter.onNext(i);
                    }

                    // Финализация только если не было отмены
                    synchronized (shouldStop) {
                        if (!shouldStop.get() && !emitter.isDisposed()) {
                            System.out.println("[Disposable] Emission completed normally");
                            emitter.onComplete();
                        }
                    }
                })
                .subscribeOn(new IOScheduler())
                .subscribe(
                        item -> System.out.println("[Disposable] Received: " + item),
                        error -> System.err.println("[Disposable] Error: " + error),
                        () -> System.out.println("[Disposable] Completed")
                );

        Thread.sleep(350);
        System.out.println("[Disposable] Disposing subscription");
        longRunning.dispose();

        // Устанавливаем флаг отмены для потока
        synchronized (shouldStop) {
            shouldStop.set(true);
        }

        // Даем время для корректного завершения
        Thread.sleep(200);
    }
}