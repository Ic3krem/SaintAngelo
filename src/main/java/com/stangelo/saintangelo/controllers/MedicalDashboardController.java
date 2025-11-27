package com.stangelo.saintangelo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.ButtonType;

public class MedicalDashboardController implements Initializable {

    // Stats Labels
    @FXML private Label totalTodayLabel;
    @FXML private Label waitingLabel;
    @FXML private Label avgWaitTimeLabel;

    // Patient Information (Treatment Tab)
    @FXML private Label patientIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label chiefComplaintLabel;
    @FXML private Label patientAgeLabel;

    // Prescription Form Fields
    @FXML private TextField medicationField;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextArea consultationNotesArea;

    // Navigation Buttons
    @FXML private Button btnCurrentPatient;
    @FXML private Button btnQueue;
    @FXML private Button btnRecords;
    @FXML private Button btnLogout;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Simulating data fetching
        if(totalTodayLabel != null) totalTodayLabel.setText("129");
        if(waitingLabel != null) waitingLabel.setText("30");
        if(avgWaitTimeLabel != null) avgWaitTimeLabel.setText("129");

        // Load current patient data if on treatment screen
        if(patientIdLabel != null) {
            patientIdLabel.setText("A145");
            patientNameLabel.setText("Patrick Claridad");
            chiefComplaintLabel.setText("Backburner");
            patientAgeLabel.setText("51");
        }
    }

    // --- NEW ACTION HANDLERS FOR QUEUE TABLE ---

    @FXML
    private void handleEscalate(ActionEvent event) {
        // Get the MenuItem that was clicked
        MenuItem item = (MenuItem) event.getSource();
        // Retrieve the Ticket ID we stored in "userData" in the FXML
        String ticketId = (String) item.getUserData();

        showAlert(Alert.AlertType.INFORMATION, "Escalate Priority",
                "Patient with Ticket " + ticketId + " has been marked as EMERGENCY.");
    }

    @FXML
    private void handleRemove(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
        String ticketId = (String) item.getUserData();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Patient " + ticketId + "?");
        confirm.setContentText("Are you sure you want to remove this patient from the queue?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Removed patient: " + ticketId);
            // In a real app, you would reload the table data here
        }
    }

    // --- EXISTING HANDLERS ---

    @FXML
    private void handleCompleteTreatment() {
        String medication = medicationField.getText();
        String dosage = dosageField.getText();
        String frequency = frequencyField.getText();

        if (medication.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all prescription details.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Treatment Complete", "Prescription saved successfully.");
        medicationField.clear();
        dosageField.clear();
        frequencyField.clear();
        consultationNotesArea.clear();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Navigate back to the Login Screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();

            // Get current stage and set the login scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            // Optional: If you want to resize window back to login size
            // stage.setWidth(800); stage.setHeight(600);
            // stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: just close the window if login file is missing
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleNavCurrentPatient(ActionEvent event) {
        // Load the main dashboard view (Current Patient)
        loadView(event, "/fxml/doctor-dashboard-view.fxml");
    }

    @FXML
    private void handleNavQueue(ActionEvent event) {
        // Load the queue management view
        loadView(event, "/fxml/doctor-queue-management.fxml");
    }

    @FXML
    private void handleNavRecords(ActionEvent event) {
        // Placeholder: reload dashboard or show alert since we don't have this FXML yet
        // loadView(event, "/fxml/patient_records.fxml");
        setActiveNav(btnRecords);
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Patient Records module is under development.");
    }

    /**
     * Helper method to switch the current scene's root to a new FXML view.
     */
    private void loadView(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the stage from the event source (the button clicked)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Switch the root of the scene (keeps the window size/state)
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load " + fxmlPath + "\nCheck if file exists in /fxml/ folder.");
        }
    }

    private void setActiveNav(Button activeButton) {
        btnCurrentPatient.getStyleClass().remove("active");
        btnQueue.getStyleClass().remove("active");
        btnRecords.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}