package com.customrx.core;

import com.customrx.disposable.CompositeDisposable;
import com.customrx.disposable.Disposable;
import com.customrx.schedulers.Scheduler;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final ObservableOnSubscribe<T> source;

    private Observable(ObservableOnSubscribe<T> source) {
        this.source = source;
    }

    public static <T> Observable<T> create(ObservableOnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<? super T> observer) {
        DefaultEmitter<T> emitter = new DefaultEmitter<>(observer);
        try {
            source.subscribe(emitter);
        } catch (Exception e) {
            emitter.onError(e);
        }
        return emitter;
    }

    public Disposable subscribe(Consumer<? super T> onNext,
                                Consumer<? super Throwable> onError,
                                Action onComplete) {
        return subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    onNext.accept(item);
                } catch (Exception e) {
                    // Теперь здесь не нужно обрабатывать Exception
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

            @Override
            public void onError(Throwable error) {
                try {
                    onError.accept(error);
                } catch (Exception e) {
                    // Обработка исключений в обработчике ошибок
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

            @Override
            public void onComplete() {
                try {
                    onComplete.run();
                } catch (Exception e) {
                    // Обработка исключений в завершении
                    Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }
        });
    }



    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return new Observable<>(emitter -> {
            Disposable disposable = this.subscribe(
                    item -> {
                        try {
                            emitter.onNext(mapper.apply(item));
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    },
                    emitter::onError,
                    emitter::onComplete
            );
        });
    }
    public Observable<T> filter(Predicate<? super T> predicate) {
        return new Observable<>(emitter -> {
            Disposable disposable = this.subscribe(
                    item -> {
                        try {
                            if (predicate.test(item)) {
                                emitter.onNext(item);
                            }
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    },
                    emitter::onError,
                    emitter::onComplete
            );
        });
    }

    public <R> Observable<R> flatMap(Function<? super T, ? extends Observable<R>> mapper) {
        return new Observable<>(emitter -> {
            CompositeDisposable composite = new CompositeDisposable();
            AtomicInteger activeSubscriptions = new AtomicInteger(1);

            Disposable mainDisposable = this.subscribe(
                    item -> {
                        try {
                            activeSubscriptions.incrementAndGet();
                            Observable<R> inner = mapper.apply(item);
                            Disposable disposable = inner.subscribe(
                                    emitter::onNext,
                                    error -> {
                                        emitter.onError(error);
                                        composite.dispose();
                                    },
                                    () -> {
                                        if (activeSubscriptions.decrementAndGet() == 0) {
                                            emitter.onComplete();
                                        }
                                    }
                            );
                            composite.add(disposable);
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    },
                    error -> {
                        emitter.onError(error);
                        composite.dispose();
                    },
                    () -> {
                        if (activeSubscriptions.decrementAndGet() == 0) {
                            emitter.onComplete();
                        }
                    }
            );

            composite.add(mainDisposable);
        });
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<>(emitter -> {
            scheduler.schedule(() -> {
                try {
                    Disposable disposable = this.subscribe(
                            emitter::onNext,
                            emitter::onError,
                            emitter::onComplete
                    );
                } catch (Exception e) {
                    emitter.onError(e);
                }
            });
        });
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<>(emitter -> {
            Disposable disposable = this.subscribe(
                    item -> scheduler.schedule(() -> {
                        if (!emitter.isDisposed()) {
                            emitter.onNext(item);
                        }
                    }),
                    error -> scheduler.schedule(() -> {
                        if (!emitter.isDisposed()) {
                            emitter.onError(error);
                        }
                    }),
                    () -> scheduler.schedule(() -> {
                        if (!emitter.isDisposed()) {
                            emitter.onComplete();
                        }
                    })
            );
        });
    }

    private static class DefaultEmitter<T> implements Emitter<T> {
        private final Observer<? super T> observer;
        private volatile boolean disposed = false;

        DefaultEmitter(Observer<? super T> observer) {
            this.observer = observer;
        }

        @Override
        public void onNext(T value) {
            if (!disposed) {
                try {
                    observer.onNext(value);
                } catch (Exception e) {
                    onError(e);
                }
            }
        }

        @Override
        public void onError(Throwable error) {
            if (!disposed) {
                try {
                    observer.onError(error);
                } finally {
                    dispose();
                }
            }
        }

        @Override
        public void onComplete() {
            if (!disposed) {
                try {
                    observer.onComplete();
                } finally {
                    dispose();
                }
            }
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
    public static <T> Observable<T> just(T item) {
        return create(emitter -> {
            emitter.onNext(item);
            emitter.onComplete();
        });
    }

}