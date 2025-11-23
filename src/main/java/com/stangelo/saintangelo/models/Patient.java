package com.stangelo.saintangelo.models;

import java.util.List;

public class Patient {
    private String id;
    private String name;
    private int age;
    private String contactNumber;
    private String homeAddress;
    private String gender;
    private String emergencycontactPerson;
    private String emergencycontactNumber;
    private boolean isSeniorCitizen;
    private String currentMedications;
    private String allergies;
    private String diagnosis;
    private String treatmentPlan;
    private String notes;
    private String roomNumber;
    private String admissionDate;
    private String dischargeDate;
    private String attendingPhysician;
    private String status;
    private String nextAppointmentDate;
    private String lastVisitDate;
    private String bloodType;
    private List<String> medicalHistory;

    public Patient(String id, String name, int age, String contactNumber, String homeAddress, String gender, String emergencycontactPerson, String emergencycontactNumber, boolean isSeniorCitizen, String currentMedications, String allergies, String diagnosis, String treatmentPlan, String notes, String roomNumber, String admissionDate, String dischargeDate, String attendingPhysician, String status, String nextAppointmentDate, String lastVisitDate, String bloodType) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.contactNumber = contactNumber;
        this.homeAddress = homeAddress;
        this.gender = gender;
        this.emergencycontactPerson = emergencycontactPerson;
        this.emergencycontactNumber = emergencycontactNumber;
        this.isSeniorCitizen = isSeniorCitizen;
        this.currentMedications = currentMedications;
        this.allergies = allergies;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
        this.notes = notes;
        this.roomNumber = roomNumber;
        this.admissionDate = admissionDate;
        this.dischargeDate = dischargeDate;
        this.attendingPhysician = attendingPhysician;
        this.status = status;

        this.nextAppointmentDate = nextAppointmentDate;
        this.lastVisitDate = lastVisitDate;
        this.bloodType = bloodType;
}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmergencycontactPerson() {
        return emergencycontactPerson;
    }

    public void setEmergencycontactPerson(String emergencycontactPerson) {
        this.emergencycontactPerson = emergencycontactPerson;
    }
    public boolean isSeniorCitizen() {
        return isSeniorCitizen;
    }

    public String getEmergencycontactNumber() {
        return emergencycontactNumber;
    }

    public void setEmergencycontactNumber(String emergencycontactNumber) {
        this.emergencycontactNumber = emergencycontactNumber;
    }

    public void setSeniorCitizen(boolean seniorCitizen) {
        isSeniorCitizen = seniorCitizen;
    }

    public String getCurrentMedications() {
        return currentMedications;
    }

    public void setCurrentMedications(String currentMedications) {
        this.currentMedications = currentMedications;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(String admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(String dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    public String getAttendingPhysician() {
        return attendingPhysician;
    }

    public void setAttendingPhysician(String attendingPhysician) {
        this.attendingPhysician = attendingPhysician;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextAppointmentDate() {
        return nextAppointmentDate;
    }

    public void setNextAppointmentDate(String nextAppointmentDate) {
        this.nextAppointmentDate = nextAppointmentDate;
    }

    public String getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(String lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public List<String> getMedicalHistory() {
        return medicalHistory;
    }

    public boolean isNewPatient(){
        return medicalHistory.isEmpty();
    }

    public void addMedicalRecord(String record) {
        this.medicalHistory.add(record);
    }
    public void removeMedicalRecord(String record) {
        this.medicalHistory.remove(record);
    }

    @Override
    public String toString() {
        String status = isNewPatient() ? "[New]" : "[Returning]";
        return name + " " + status + " (History: " + medicalHistory.size() + " records)";
    }

    public void setMedicalHistory(List<String> medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
};
