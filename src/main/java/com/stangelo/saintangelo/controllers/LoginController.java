package com.stangelo.saintangelo.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

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

                // 1. Set Initial Opacity to 0 (Invisible) for Animation
                dashboardRoot.setOpacity(0);

                Stage dashboardStage = new Stage();
                dashboardStage.setTitle(dashboardTitle);
                dashboardStage.setScene(new Scene(dashboardRoot));

                // --- FULL SCREEN LOGIC START ---
                // Get the visual bounds of the primary screen (excluding taskbar)
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

                dashboardStage.setX(bounds.getMinX());
                dashboardStage.setY(bounds.getMinY());
                dashboardStage.setWidth(bounds.getWidth());
                dashboardStage.setHeight(bounds.getHeight());

                // Also set maximized flag to ensure OS handles it correctly
                dashboardStage.setMaximized(true);
                // --- FULL SCREEN LOGIC END ---

                dashboardStage.show();

                // 2. Play Fade Transition (0.0 -> 1.0)
                FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), dashboardRoot);
                fadeTransition.setFromValue(0.0);
                fadeTransition.setToValue(1.0);
                fadeTransition.play();

                // Close the login window
                Stage loginStage = (Stage) loginButton.getScene().getWindow();
                loginStage.close();

                return true;

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load the dashboard.\nCheck that " + fxmlFile + " exists in resources.");
                return false;
            }
        }

        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}