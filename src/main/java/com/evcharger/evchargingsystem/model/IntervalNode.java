package com.evcharger.evchargingsystem.model;

import java.time.Instant;

public class IntervalNode {
    public Instant start;
    public Instant end;
    public Instant maxEnd; // Tracks highest end-time in subtree for O(log N) pruning
    public String reservationId;
    public String bayId;
    public IntervalNode left;
    public IntervalNode right;

    public IntervalNode(Instant start, Instant end, String reservationId, String bayId) {
        this.start = start;
        this.end = end;
        this.maxEnd = end;
        this.reservationId = reservationId;
        this.bayId = bayId;
    }
}