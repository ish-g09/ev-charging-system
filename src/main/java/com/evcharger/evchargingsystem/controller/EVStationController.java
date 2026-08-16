package com.evcharger.evchargingsystem.controller;

import com.evcharger.evchargingsystem.dto.ReservationRequest;
import com.evcharger.evchargingsystem.model.ChargingSession;
import com.evcharger.evchargingsystem.observer.UserNotificationObserver;
import com.evcharger.evchargingsystem.service.ConcurrentIntervalTreeService;
import com.evcharger.evchargingsystem.service.GridLoadBalancerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@RestController
@RequestMapping("/api/v1/ev")
public class EVStationController {

    private final ConcurrentIntervalTreeService treeService;
    private final GridLoadBalancerService loadBalancerService;

    // Dedicated Worker Pool for background charging tasks (5 worker threads)
    private final ExecutorService chargingWorkerPool = Executors.newFixedThreadPool(5);

    // Thread-safe map tracking active charging sessions: sessionId -> ChargingSession
    private final Map<String, ChargingSession> activeSessionsMap = new ConcurrentHashMap<>();

    // Constructor Injection (Spring automatically injects these Beans)
    public EVStationController(ConcurrentIntervalTreeService treeService, GridLoadBalancerService loadBalancerService) {
        this.treeService = treeService;
        this.loadBalancerService = loadBalancerService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSlot(@RequestBody ReservationRequest request) {
        Instant start = Instant.parse(request.getStartTimeIso());
        Instant end = Instant.parse(request.getEndTimeIso());

        boolean booked = treeService.reserveSlot(start, end, request.getReservationId(), request.getBayId());

        if (!booked) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("[CONFLICT] Slot booking failed: Requested interval overlaps with an existing reservation.");
        }

        return ResponseEntity.ok("[SUCCESS] Slot reserved for ID: " + request.getReservationId());
    }

    @PostMapping("/start-session")
    public ResponseEntity<String> startSession(@RequestParam String sessionId, @RequestParam int initialSoc) {
        ChargingSession session = new ChargingSession(sessionId, initialSoc, List.of(new UserNotificationObserver()));
        activeSessionsMap.put(sessionId, session);

        // Submit task to background thread pool (Non-blocking HTTP execution)
        chargingWorkerPool.submit(session);

        return ResponseEntity.ok("[ASYNC] Session " + sessionId + " started in background worker pool.");
    }

    @GetMapping("/grid-status")
    public ResponseEntity<Map<String, Double>> getGridStatus() {
        Map<String, Integer> currentSocMap = new HashMap<>();

        for (Map.Entry<String, ChargingSession> entry : activeSessionsMap.entrySet()) {
            if (entry.getValue().isActive()) {
                currentSocMap.put(entry.getKey(), entry.getValue().getBatterySoc());
            }
        }

        return ResponseEntity.ok(loadBalancerService.calculatePowerAllocation(currentSocMap));
    }
}