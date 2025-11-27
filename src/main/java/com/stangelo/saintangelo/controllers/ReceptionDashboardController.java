package com.stangelo.saintangelo.controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ReceptionDashboardController implements Initializable {

    // --- FXML INJECTIONS FOR REGISTRATION TABS ---
    @FXML private Button btnTabNew;
    @FXML private Button btnTabExisting;
    @FXML private VBox formNewPatient;
    @FXML private VBox formExistingPatient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default initialization if needed
    }

    // --- REGISTRATION TAB HANDLERS ---

    @FXML
    private void handleTabNewPatient(ActionEvent event) {
        // 1. Update Styles
        btnTabNew.getStyleClass().removeAll("reg-tab-inactive");
        btnTabNew.getStyleClass().add("reg-tab-active");

        btnTabExisting.getStyleClass().removeAll("reg-tab-active");
        btnTabExisting.getStyleClass().add("reg-tab-inactive");

        // 2. Switch Visibility
        if (formNewPatient != null && formExistingPatient != null) {
            formNewPatient.setVisible(true);
            formNewPatient.setManaged(true);

            formExistingPatient.setVisible(false);
            formExistingPatient.setManaged(false);
        }
    }

    @FXML
    private void handleTabExistingPatient(ActionEvent event) {
        // 1. Update Styles
        btnTabExisting.getStyleClass().removeAll("reg-tab-inactive");
        btnTabExisting.getStyleClass().add("reg-tab-active");

        btnTabNew.getStyleClass().removeAll("reg-tab-active");
        btnTabNew.getStyleClass().add("reg-tab-inactive");

        // 2. Switch Visibility
        if (formNewPatient != null && formExistingPatient != null) {
            formExistingPatient.setVisible(true);
            formExistingPatient.setManaged(true);

            formNewPatient.setVisible(false);
            formNewPatient.setManaged(false);
        }
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        loadView(event, "/fxml/receptionist-dashboard-view.fxml");
    }

    @FXML
    private void handleNavRegistration(ActionEvent event) {
        loadView(event, "/fxml/receptionist-registration-view.fxml");
    }

    @FXML
    private void handleNavQueue(ActionEvent event) {
        loadView(event, "/fxml/receptionist-queueManagement-view.fxml");
    }

    @FXML
    private void handleNavAppointments(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Appointments module is under development.");
    }

    @FXML
    private void handleNavDischarge(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Discharge module is under development.");
    }

    // --- LOGOUT HANDLER ---

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent loginView = loader.load();

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            HBox titleBar = new HBox();
            titleBar.setAlignment(Pos.CENTER_LEFT);
            titleBar.setPadding(new Insets(10, 5, 5, 10));
            titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 30 30 0 0;");

            Label titleLabel = new Label("Saint Angelo Medical Center");
            titleLabel.setTextFill(Color.WHITE);

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox controlButtons = new HBox(10);
            controlButtons.setAlignment(Pos.CENTER);

            Button minimizeButton = new Button("—");
            minimizeButton.getStyleClass().addAll("title-bar-button", "minimize-button");
            minimizeButton.setOnAction(e -> loginStage.setIconified(true));

            Button closeButton = new Button("X");
            closeButton.getStyleClass().addAll("title-bar-button", "close-button");
            closeButton.setOnAction(e -> loginStage.close());

            controlButtons.getChildren().addAll(minimizeButton, closeButton);
            titleBar.getChildren().addAll(titleLabel, spacer, controlButtons);
            titleBar.setPadding(new Insets(10, 40, 10, 10));

            final double[] xOffset = {0};
            final double[] yOffset = {0};
            titleBar.setOnMousePressed(e -> {
                xOffset[0] = e.getSceneX();
                yOffset[0] = e.getSceneY();
            });
            titleBar.setOnMouseDragged(e -> {
                loginStage.setX(e.getScreenX() - xOffset[0]);
                loginStage.setY(e.getScreenY() - yOffset[0]);
            });

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: transparent;");
            root.setTop(titleBar);
            root.setCenter(loginView);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());

            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- HELPER METHODS ---

    private void loadView(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            root.setOpacity(0);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), root);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load " + fxmlPath + "\nCheck if file exists in /fxml/ folder.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}