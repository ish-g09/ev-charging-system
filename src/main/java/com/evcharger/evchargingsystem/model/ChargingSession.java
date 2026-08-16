package com.evcharger.evchargingsystem.model;

import com.evcharger.evchargingsystem.observer.ChargingObserver;
import java.util.List;

public class ChargingSession implements Runnable {

    private final String sessionId;
    private int batterySoc;
    private final List<ChargingObserver> observers;
    private volatile boolean active = true;

    public ChargingSession(String sessionId, int initialSoc, List<ChargingObserver> observers) {
        this.sessionId = sessionId;
        this.batterySoc = initialSoc;
        this.observers = observers;
    }

    public String getSessionId() { return sessionId; }
    public int getBatterySoc() { return batterySoc; }
    public boolean isActive() { return active; }

    @Override
    public void run() {
        while (active && batterySoc < 100) {
            try {
                Thread.sleep(2000); // Simulate time passage (2 seconds = +10% charge)
                batterySoc = Math.min(100, batterySoc + 10);

                // Notify observers of battery progression
                for (ChargingObserver obs : observers) {
                    obs.onBatteryUpdate(sessionId, batterySoc);
                }

                if (batterySoc >= 100) {
                    active = false;
                    for (ChargingObserver obs : observers) {
                        obs.onSessionCompleted(sessionId);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                active = false;
                break;
            }
        }
    }
}