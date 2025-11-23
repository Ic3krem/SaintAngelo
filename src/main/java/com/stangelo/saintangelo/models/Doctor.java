package com.stangelo.saintangelo.models;

public class Doctor {
    private String id;
    private String name;
    private String specialization;
    private int buildingNumber;

    public Doctor(String id, String name, String specialization, int buildingNumber){
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.buildingNumber = buildingNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public int getBuildingNumber() { return buildingNumber; }
    public void setBuildingNumber(int buildingNumber) { this.buildingNumber = buildingNumber; }

    @Override
    public String toString() { return "Dr. " + name + " (Specialization: " +
            specialization + ", Building: " + buildingNumber + ", ID: " + id + ")"; }
};
