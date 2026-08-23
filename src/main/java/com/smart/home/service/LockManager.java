package com.smart.home.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class LockManager {

    private final Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T executeWithLock(Long resourceId, Supplier<T> action) {
        ReentrantLock lock = locks.computeIfAbsent(resourceId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    public void executeWithLock(Long resourceId, Runnable action) {
        ReentrantLock lock = locks.computeIfAbsent(resourceId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }

    public void removeLock(Long resourceId) {
        locks.remove(resourceId);
    }
}
