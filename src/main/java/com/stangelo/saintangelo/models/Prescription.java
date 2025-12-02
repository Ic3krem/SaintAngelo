package com.stangelo.saintangelo.models;

import java.time.LocalDateTime;

/**
 * Prescription model class
 */
public class Prescription {
    private String prescriptionId;
    private Patient patient;
    private Doctor doctor;
    private String medication;
    private String dosage;
    private String frequency;
    private String consultationNotes;
    private LocalDateTime consultationDate;
    private String diagnosis;
    private String treatmentPlan;

    public Prescription(String prescriptionId, Patient patient, Doctor doctor, String medication,
                        String dosage, String frequency, String consultationNotes, LocalDateTime consultationDate,
                        String diagnosis, String treatmentPlan) {
        this.prescriptionId = prescriptionId;
        this.patient = patient;
        this.doctor = doctor;
        this.medication = medication;
        this.dosage = dosage;
        this.frequency = frequency;
        this.consultationNotes = consultationNotes;
        this.consultationDate = consultationDate;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
    }

    // Getters and Setters
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getConsultationNotes() { return consultationNotes; }
    public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }

    public LocalDateTime getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
}

