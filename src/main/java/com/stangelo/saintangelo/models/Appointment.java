package com.stangelo.saintangelo.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment model class
 * Represents a scheduled appointment between a patient and doctor
 */
public class Appointment {
    private String appointmentId;
    private Patient patient;
    private Doctor doctor;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String purpose;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private String notes;

    public Appointment(String appointmentId, Patient patient, Doctor doctor, LocalDate appointmentDate,
                       LocalTime appointmentTime, String purpose, AppointmentStatus status, String notes) {
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.purpose = purpose;
        this.status = status;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

