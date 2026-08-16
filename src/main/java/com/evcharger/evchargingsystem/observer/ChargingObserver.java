package com.evcharger.evchargingsystem.observer;

public interface ChargingObserver {
    void onBatteryUpdate(String sessionId, int soc);
    void onSessionCompleted(String sessionId);
}