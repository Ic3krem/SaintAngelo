package com.stangelo.saintangelo.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class ReceptionistController {

    @FXML
    private Button btnLogout;

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent root = loader.load();

            // Create a new stage for the login window
            Stage loginStage = new Stage();
            loginStage.setTitle("Login");
            loginStage.setScene(new Scene(root));

            // Get the current stage (the dashboard) and close it
            Stage dashboardStage = (Stage) btnLogout.getScene().getWindow();
            dashboardStage.close();

            // Show the login stage
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception, maybe show an alert
        }
    }
}
