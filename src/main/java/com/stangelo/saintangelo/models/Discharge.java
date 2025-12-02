package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;

/**
 * Discharge model class
 */
public class Discharge {
    private String dischargeId;
    private Patient patient;
    private String department;
    private DischargeStatus status;
    private LocalDateTime dischargeDate;
    private Prescription prescription;
    private String billingAmount;
    private String notes;

    public Discharge(String dischargeId, Patient patient, String department, DischargeStatus status,
                     LocalDateTime dischargeDate, Prescription prescription, String billingAmount, String notes) {
        this.dischargeId = dischargeId;
        this.patient = patient;
        this.department = department;
        this.status = status;
        this.dischargeDate = dischargeDate;
        this.prescription = prescription;
        this.billingAmount = billingAmount;
        this.notes = notes;
    }

    // Getters and Setters
    public String getDischargeId() { return dischargeId; }
    public void setDischargeId(String dischargeId) { this.dischargeId = dischargeId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public DischargeStatus getStatus() { return status; }
    public void setStatus(DischargeStatus status) { this.status = status; }

    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }

    public Prescription getPrescription() { return prescription; }
    public void setPrescription(Prescription prescription) { this.prescription = prescription; }

    public String getBillingAmount() { return billingAmount; }
    public void setBillingAmount(String billingAmount) { this.billingAmount = billingAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

