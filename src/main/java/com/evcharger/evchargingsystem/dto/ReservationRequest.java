package com.evcharger.evchargingsystem.dto;

public class ReservationRequest {
    private String reservationId;
    private String bayId;
    private String startTimeIso; // e.g., "2026-08-16T10:00:00Z"
    private String endTimeIso;   // e.g., "2026-08-16T12:00:00Z"

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getBayId() { return bayId; }
    public void setBayId(String bayId) { this.bayId = bayId; }

    public String getStartTimeIso() { return startTimeIso; }
    public void setStartTimeIso(String startTimeIso) { this.startTimeIso = startTimeIso; }

    public String getEndTimeIso() { return endTimeIso; }
    public void setEndTimeIso(String endTimeIso) { this.endTimeIso = endTimeIso; }
}