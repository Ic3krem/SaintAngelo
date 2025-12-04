package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String visitId;
    private String ticketNumber;
    private Patient patient;
    private TicketStatus status;
    private PriorityLevel priority;
    private LocalDateTime createdTime;
    private LocalDateTime calledTime;
    private String serviceType;
    private String assignedDoctorId;
    private String assignedDoctorName;

    // Constructor for new tickets (without doctor)
    public Ticket(String visitId, String ticketNumber, Patient patient, TicketStatus status, LocalDateTime createdTime, String serviceType) {
        this.visitId = visitId;
        this.ticketNumber = ticketNumber;
        this.patient = patient;
        this.status = status;
        this.priority = PriorityLevel.REGULAR;
        this.createdTime = createdTime;
        this.serviceType = serviceType;
        this.assignedDoctorId = null;
        this.assignedDoctorName = null;
    }

    // Full constructor with all fields
    public Ticket(String visitId, String ticketNumber, Patient patient, TicketStatus status, 
                  PriorityLevel priority, LocalDateTime createdTime, LocalDateTime calledTime,
                  String serviceType, String assignedDoctorId, String assignedDoctorName) {
        this.visitId = visitId;
        this.ticketNumber = ticketNumber;
        this.patient = patient;
        this.status = status;
        this.priority = priority != null ? priority : PriorityLevel.REGULAR;
        this.createdTime = createdTime;
        this.calledTime = calledTime;
        this.serviceType = serviceType;
        this.assignedDoctorId = assignedDoctorId;
        this.assignedDoctorName = assignedDoctorName;
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

    public PriorityLevel getPriority() {
        return priority;
    }

    public void setPriority(PriorityLevel priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getCalledTime() {
        return calledTime;
    }

    public void setCalledTime(LocalDateTime calledTime) {
        this.calledTime = calledTime;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAssignedDoctorId() {
        return assignedDoctorId;
    }

    public void setAssignedDoctorId(String assignedDoctorId) {
        this.assignedDoctorId = assignedDoctorId;
    }

    public String getAssignedDoctorName() {
        return assignedDoctorName;
    }

    public void setAssignedDoctorName(String assignedDoctorName) {
        this.assignedDoctorName = assignedDoctorName;
    }

    public boolean hasAssignedDoctor() {
        return assignedDoctorId != null && !assignedDoctorId.isEmpty();
    }

    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return createdTime.format(formatter);
    }

    public String getFormattedCalledTime() {
        if (calledTime == null) return "---";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return calledTime.format(formatter);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "visitId='" + visitId + '\'' +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", status=" + status +
                ", doctor=" + (assignedDoctorName != null ? assignedDoctorName : "Not Assigned") +
                '}';
    }
}
