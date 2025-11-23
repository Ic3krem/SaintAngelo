package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String visitId;
    private String ticketNumber;
    private Patient patient;
    private TicketStatus status;
    private LocalDateTime createdTime;
    private String serviceType;

    public Ticket(String visitId, String ticketNumber, Patient patient, TicketStatus status, LocalDateTime createdTime, String serviceType) {
        this.visitId = visitId;
        this.ticketNumber = ticketNumber;
        this.patient = patient;
        this.status = status;
        this.createdTime = createdTime;
        this.serviceType = serviceType;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return createdTime.format(formatter);
    }
    @Override
    public String toString() {
        return "Ticket{" +
                "visitId='" + visitId + '\'' +
                ", ticketNumber='" + ticketNumber + '\'' +
                '}';
    }
}
