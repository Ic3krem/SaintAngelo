package com.stangelo.saintangelo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    public void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // --- Simple, Hardcoded Authentication ---
        // In a real app, you would use an AuthService to check credentials against a database.
        if (isValid(username, password)) {
            errorLabel.setText(""); // Clear error message
            System.out.println("Login Successful!");
            loadDashboard();
        } else {
            errorLabel.setText("Invalid username or password. Please try again.");
            System.out.println("Login Failed.");
        }
    }

    /**
     * A basic, placeholder validation method.
     * Replace this with a call to a real authentication service.
     */
    private boolean isValid(String username, String password) {
        // For testing purposes, let's use a simple check.
        // A real app would query a database.
        return "admin".equals(username) && "password".equals(password);
    }

    /**
     * Loads the main application dashboard after a successful login.
     */
    private void loadDashboard() {
        try {
            // Load the dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
            Parent dashboardRoot = loader.load();

            // Create a new scene and stage
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("St. Angelo Dashboard");
            dashboardStage.setScene(new Scene(dashboardRoot));

            // Show the dashboard
            dashboardStage.show();

            // Close the login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load the dashboard.");
        }
    }
}
