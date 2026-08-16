package com.evcharger.evchargingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentIntervalTreeServiceTest {

    private ConcurrentIntervalTreeService service;

    @BeforeEach
    void setUp() {
        service = new ConcurrentIntervalTreeService();
    }

    @Test
    void testConcurrentBookingsPreventDoubleBooking() throws InterruptedException {
        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1); // Synchronizes thread start
        AtomicInteger successfulBookings = new AtomicInteger(0);

        Instant start = Instant.parse("2026-08-16T10:00:00Z");
        Instant end = Instant.parse("2026-08-16T12:00:00Z");

        for (int i = 0; i < numberOfThreads; i++) {
            final String resId = "RES-" + i;
            executor.submit(() -> {
                try {
                    latch.await(); // All threads wait here until latch releases
                    boolean booked = service.reserveSlot(start, end, resId, "BAY-1");
                    if (booked) {
                        successfulBookings.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown(); // Releases all 50 threads simultaneously
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Exactly ONE thread should successfully claim the overlapping time range
        assertEquals(1, successfulBookings.get(), "Zero double-booking guarantee breached!");
    }
}