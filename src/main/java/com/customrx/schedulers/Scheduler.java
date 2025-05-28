package com.customrx.schedulers;

public interface Scheduler {
    void schedule(Runnable task);
    void shutdown();
}
