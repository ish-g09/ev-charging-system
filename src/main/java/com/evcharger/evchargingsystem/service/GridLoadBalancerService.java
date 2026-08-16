package com.evcharger.evchargingsystem.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GridLoadBalancerService {

    private static final double MAX_GRID_CAPACITY_KW = 100.0; // 100 kW grid cap

    /**
     * Dynamically calculates power allocation per vehicle using a two-pass greedy strategy.
     * @param activeVehiclesSoc Map of sessionId -> battery percentage (e.g., "SESS-1" -> 85)
     * @return Map of sessionId -> allocated power in kW
     */
    public Map<String, Double> calculatePowerAllocation(Map<String, Integer> activeVehiclesSoc) {
        Map<String, Double> powerAllocationMap = new ConcurrentHashMap<>();
        if (activeVehiclesSoc.isEmpty()) {
            return powerAllocationMap;
        }

        double remainingPowerKw = MAX_GRID_CAPACITY_KW;
        List<String> lowSocVehicleIds = new ArrayList<>();

        // Phase 1: Throttle vehicles with battery >= 80%
        for (Map.Entry<String, Integer> entry : activeVehiclesSoc.entrySet()) {
            String sessionId = entry.getKey();
            int currentSoc = entry.getValue();

            if (currentSoc >= 80) {
                double throttledPower = 10.0; // Fixed minimal power for trickle charging
                powerAllocationMap.put(sessionId, throttledPower);
                remainingPowerKw -= throttledPower;
            } else {
                lowSocVehicleIds.add(sessionId);
            }
        }

        // Phase 2: Divide remaining power equally among low SoC vehicles (< 80%)
        if (!lowSocVehicleIds.isEmpty()) {
            double powerPerLowSocVehicle = Math.max(0.0, remainingPowerKw / lowSocVehicleIds.size());
            for (String sessionId : lowSocVehicleIds) {
                powerAllocationMap.put(sessionId, powerPerLowSocVehicle);
            }
        }

        return powerAllocationMap;
    }
}