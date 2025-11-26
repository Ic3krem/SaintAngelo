package com.stangelo.saintangelo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class MedicalDashboardController implements Initializable {

    // Stats Labels
    @FXML private Label totalTodayLabel;
    @FXML private Label waitingLabel;
    @FXML private Label avgWaitTimeLabel;

    // Patient Information
    @FXML private Label patientIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label chiefComplaintLabel;
    @FXML private Label patientAgeLabel;

    // Prescription Form Fields
    @FXML private TextField medicationField;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextArea consultationNotesArea;

    // Navigation Buttons (for active state handling)
    @FXML private Button btnCurrentPatient;
    @FXML private Button btnQueue;
    @FXML private Button btnRecords;
    @FXML private Button btnLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with default data or fetch from backend
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Simulating data fetching
        totalTodayLabel.setText("129");
        waitingLabel.setText("30");
        avgWaitTimeLabel.setText("129"); // Assuming minutes or index

        // Load current patient data
        patientIdLabel.setText("A145");
        patientNameLabel.setText("Patrick Claridad");
        chiefComplaintLabel.setText("Backburner"); // From UI image
        patientAgeLabel.setText("51");
    }

    @FXML
    private void handleCompleteTreatment() {
        String medication = medicationField.getText();
        String dosage = dosageField.getText();
        String frequency = frequencyField.getText();
        String notes = consultationNotesArea.getText();

        if (medication.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all prescription details.");
            return;
        }

        // Logic to save treatment to database would go here
        System.out.println("Completing treatment for: " + patientNameLabel.getText());
        System.out.println("Rx: " + medication + " " + dosage + " " + frequency);
        System.out.println("Notes: " + notes);

        // Show success message
        showAlert(Alert.AlertType.INFORMATION, "Treatment Complete", "Prescription and notes have been saved.");

        // Clear form
        clearForm();
    }

    @FXML
    private void handleLogout() {
        System.out.println("Logging out user...");
        // Logic to switch scenes to login screen
    }

    @FXML
    private void handleNavCurrentPatient() {
        setActiveNav(btnCurrentPatient);
        // Logic to switch view to Current Patient
    }

    @FXML
    private void handleNavQueue() {
        setActiveNav(btnQueue);
        // Logic to switch view to Queue Management
    }

    @FXML
    private void handleNavRecords() {
        setActiveNav(btnRecords);
        // Logic to switch view to Patient Records
    }

    private void setActiveNav(Button activeButton) {
        // Reset all styles to default
        btnCurrentPatient.getStyleClass().remove("active");
        btnQueue.getStyleClass().remove("active");
        btnRecords.getStyleClass().remove("active");

        // Set active style
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void clearForm() {
        medicationField.clear();
        dosageField.clear();
        frequencyField.clear();
        consultationNotesArea.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}