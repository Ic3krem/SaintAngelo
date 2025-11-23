package com.stangelo.saintangelo.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class RegistrationController {

    // Form Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField ageField;
    @FXML private TextArea complaintArea;
    @FXML private ComboBox<String> priorityComboBox;

    // Views within the StackPane
    @FXML private ScrollPane registrationScrollView;
    @FXML private VBox ticketView;

    // Ticket View Fields
    @FXML private Label ticketNumberLabel;

    @FXML
    public void initialize() {
        // Initialize the ComboBox with priority levels
        priorityComboBox.getItems().addAll("Regular", "Senior Citizen", "Emergency");
        
        // Ensure the registration form is visible by default and the ticket view is hidden
        registrationScrollView.setVisible(true);
        ticketView.setVisible(false);
    }

    @FXML
    private void handleGetQueueNumber() {
        // 1. Get data from form fields
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        // ... get other fields ...

        // 2. In a real app, you would create a Patient object and use the QueueService
        // For now, we'll just simulate it.
        System.out.println("Registering patient: " + firstName + " " + lastName);
        String generatedTicketNumber = "A054"; // Simulate a new ticket number

        // 3. Update the ticket view with the new number
        ticketNumberLabel.setText(generatedTicketNumber);

        // 4. Switch the view
        showTicketView();
    }

    @FXML
    private void handleRegisterNewTicket() {
        // This method is called by the "Register New Ticket" button on the ticket view
        
        // 1. Clear the form fields
        firstNameField.clear();
        lastNameField.clear();
        phoneNumberField.clear();
        ageField.clear();
        complaintArea.clear();
        priorityComboBox.getSelectionModel().clearSelection();

        // 2. Switch back to the registration form
        showRegistrationForm();
    }

    private void showRegistrationForm() {
        registrationScrollView.setVisible(true);
        ticketView.setVisible(false);
    }

    private void showTicketView() {
        registrationScrollView.setVisible(false);
        ticketView.setVisible(true);
    }
}
