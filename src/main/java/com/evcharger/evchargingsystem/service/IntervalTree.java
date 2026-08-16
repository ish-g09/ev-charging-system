package com.evcharger.evchargingsystem.service;

import com.evcharger.evchargingsystem.model.IntervalNode;
import java.time.Instant;

public class IntervalTree {
    private IntervalNode root;

    public boolean isOverlapping(Instant start, Instant end) {
        return checkOverlap(root, start, end);
    }

    private boolean checkOverlap(IntervalNode node, Instant start, Instant end) {
        if (node == null) return false;

        // Overlap condition: start < node.end AND node.start < end
        if (start.isBefore(node.end) && node.start.isBefore(end)) {
            return true;
        }

        // Pruning logic: if left child maxEnd > start, search left branch
        if (node.left != null && node.left.maxEnd.isAfter(start)) {
            return checkOverlap(node.left, start, end);
        }

        return checkOverlap(node.right, start, end);
    }

    public void insert(Instant start, Instant end, String reservationId, String bayId) {
        root = insertNode(root, start, end, reservationId, bayId);
    }

    private IntervalNode insertNode(IntervalNode node, Instant start, Instant end, String reservationId, String bayId) {
        if (node == null) {
            return new IntervalNode(start, end, reservationId, bayId);
        }

        if (start.isBefore(node.start)) {
            node.left = insertNode(node.left, start, end, reservationId, bayId);
        } else {
            node.right = insertNode(node.right, start, end, reservationId, bayId);
        }

        if (node.maxEnd.isBefore(end)) {
            node.maxEnd = end;
        }

        return node;
    }
}