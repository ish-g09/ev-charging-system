package com.evcharger.evchargingsystem.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ConcurrentIntervalTreeService {

    private final IntervalTree intervalTree = new IntervalTree();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Reserve a slot safely across multiple concurrent threads
     */
    public boolean reserveSlot(Instant start, Instant end, String reservationId, String bayId) {
        // Step A: Acquire Exclusive Write Lock (Blocks all reads/writes during insertion)
        lock.writeLock().lock();
        try {
            // Step B: Check overlap while holding the write lock to eliminate race conditions
            if (intervalTree.isOverlapping(start, end)) {
                return false; // Slot already occupied
            }
            // Step C: Insert into Tree
            intervalTree.insert(start, end, reservationId, bayId);
            return true;
        } finally {
            // Step D: Always unlock in a finally block to prevent deadlocks
            lock.writeLock().unlock();
        }
    }

    /**
     * Read-only operation: multiple threads can query availability simultaneously
     */
    public boolean checkAvailability(Instant start, Instant end) {
        lock.readLock().lock();
        try {
            return !intervalTree.isOverlapping(start, end);
        } finally {
            lock.readLock().unlock();
        }
    }
}