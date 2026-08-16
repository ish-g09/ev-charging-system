package com.evcharger.evchargingsystem.observer;

public class UserNotificationObserver implements ChargingObserver {

    @Override
    public void onBatteryUpdate(String sessionId, int soc) {
        if (soc == 80) {
            System.out.println("[EVENT ALERT] Session " + sessionId + ": Reached 80% SoC. Greedy Throttling activated to protect battery health.");
        }
    }

    @Override
    public void onSessionCompleted(String sessionId) {
        System.out.println("[EVENT ALERT] Session " + sessionId + ": Charging Completed (100%). Bay liberated for next user.");
    }
}