package com.customrx;

import com.customrx.core.*;
import com.customrx.core.Observable;
import com.customrx.core.Observer;
import com.customrx.schedulers.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class SchedulersTest {


    @Test
    void testSubscribeOn() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> emitThread = new AtomicReference<>();
        final AtomicBoolean isNotMainThread = new AtomicBoolean(false);

        Observable.create((Emitter<String> emitter) -> {
                    emitThread.set(Thread.currentThread().getName());
                    System.out.println("Emit thread: " + emitThread.get());
                    emitter.onNext("test");
                    emitter.onComplete();
                })
                .subscribeOn(new IOScheduler())
                .subscribe(
                        item -> isNotMainThread.set(!Thread.currentThread().getName().equals("main")),
                        error -> latch.countDown(),
                        latch::countDown
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(emitThread.get());
        // Правильная проверка для IO потока
        assertTrue(emitThread.get().startsWith("io-thread-"),
                "Emit thread should start with 'io-thread-', but was: " + emitThread.get());
        assertTrue(isNotMainThread.get(), "Processing should not be in main thread");
    }

    @Test
    void testObserveOn() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> processThread = new AtomicReference<>();
        final AtomicBoolean isComputationThread = new AtomicBoolean(false);

        Observable.create((Emitter<String> emitter) -> {
                    emitter.onNext("data");
                    emitter.onComplete();
                })
                .observeOn(new ComputationScheduler())
                .subscribe(
                        item -> {
                            processThread.set(Thread.currentThread().getName());
                            System.out.println("Process thread: " + processThread.get());
                            // Правильная проверка для Computation потока
                            isComputationThread.set(processThread.get().startsWith("computation-thread-"));
                        },
                        error -> latch.countDown(),
                        latch::countDown
                );

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertNotNull(processThread.get());
        // Правильная проверка для Computation потока
        assertTrue(processThread.get().startsWith("computation-thread-"),
                "Process thread should start with 'computation-thread-', but was: " + processThread.get());
        assertTrue(isComputationThread.get(), "Should be processed in computation thread");
    }

    @Test
    void testThreadSwitching() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> emitThread = new AtomicReference<>();
        AtomicReference<String> processThread = new AtomicReference<>();

        Observable.create((Emitter<String> emitter) -> {
                    emitThread.set(Thread.currentThread().getName());
                    emitter.onNext("test");
                    emitter.onComplete();
                })
                .subscribeOn(new IOScheduler())
                .observeOn(new ComputationScheduler())
                .map(s -> s.toUpperCase())
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        processThread.set(Thread.currentThread().getName());
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable error) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(3, TimeUnit.SECONDS), "Timeout waiting for events");

        System.out.println("Emit thread: " + emitThread.get());
        System.out.println("Process thread: " + processThread.get());

        assertNotNull(emitThread.get(), "Emit thread not set");
        assertNotNull(processThread.get(), "Process thread not set");

        // Обновленные проверки с учетом новых имен потоков
        assertTrue(emitThread.get().contains("io"),
                "Emit thread should contain 'io' but was: " + emitThread.get());

        assertTrue(processThread.get().contains("single"),
                "Process thread should contain 'single' but was: " + processThread.get());

        assertNotEquals(emitThread.get(), processThread.get(),
                "Threads should be different");
    }

    @Test
    void testComputationScheduler() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> threadName = new AtomicReference<>();

        Observable.create((Emitter<Integer> emitter) -> {
                    emitter.onNext(42);
                    emitter.onComplete();
                })
                .subscribeOn(new ComputationScheduler())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        threadName.set(Thread.currentThread().getName());
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable error) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for event");

        System.out.println("Computation thread: " + threadName.get());
        assertNotNull(threadName.get(), "Thread name not set");
        assertTrue(threadName.get().contains("computation"),
                "Thread name should contain 'computation' but was: " + threadName.get());
    }
}
