package com.stangelo.saintangelo.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private Button loginButton;

    @FXML
    public void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Directly attempt to load the dashboard based on credentials
        if (!loadDashboardForCredentials(username, password)) {
            showAlert("Login Failed", "Invalid username or password. Please try again.");
            System.out.println("Login Failed.");
        }
    }

    private boolean loadDashboardForCredentials(String username, String password) {
        String fxmlFile = null;
        String dashboardTitle = null;

        if ("reception".equals(username) && "password".equals(password)) {
            fxmlFile = "/fxml/receptionist-dashboard-view.fxml";
            dashboardTitle = "Receptionist Dashboard";
        } else if ("doctor".equals(username) && "password".equals(password)) {
            fxmlFile = "/fxml/doctor-dashboard-view.fxml";
            dashboardTitle = "Doctor Dashboard";
        }

        if (fxmlFile != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Parent dashboardRoot = loader.load();

                Stage dashboardStage = new Stage();
                dashboardStage.setTitle(dashboardTitle);
                dashboardStage.setScene(new Scene(dashboardRoot));
                dashboardStage.show();

                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                return true;

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load the dashboard.");
                return false;
            }
        }

        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait();
    }
}
