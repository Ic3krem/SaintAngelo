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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration; // Import for Animation Duration

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
        if(totalTodayLabel != null) totalTodayLabel.setText("129");
        if(waitingLabel != null) waitingLabel.setText("30");
        if(avgWaitTimeLabel != null) avgWaitTimeLabel.setText("129");

        if(patientIdLabel != null) {
            patientIdLabel.setText("A145");
            patientNameLabel.setText("Patrick Claridad");
            chiefComplaintLabel.setText("Backburner");
            patientAgeLabel.setText("51");
        }
    }

    // --- ACTION HANDLERS FOR QUEUE TABLE ---

    @FXML
    private void handleEscalate(ActionEvent event) {
        MenuItem item = (MenuItem) event.getSource();
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
        }
    }

    // --- TREATMENT HANDLERS ---

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

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Close current dashboard
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Load Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent loginView = loader.load();

            // ANIMATION: Set initial opacity to 0
            loginView.setOpacity(0);

            // Create New Stage for Login
            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            // Reconstruct Title Bar
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

            // ANIMATION: Fade In Login Screen
            FadeTransition fade = new FadeTransition(Duration.millis(500), loginView);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (IOException e) {
            e.printStackTrace();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleNavCurrentPatient(ActionEvent event) {
        loadView(event, "/fxml/doctor-dashboard-view.fxml");
    }

    @FXML
    private void handleNavQueue(ActionEvent event) {
        loadView(event, "/fxml/doctor-queue-management.fxml");
    }

    @FXML
    private void handleNavRecords(ActionEvent event) {
        loadView(event, "/fxml/doctor-patient-records.fxml");
    }

    /**
     * Helper method to switch the current scene's root to a new FXML view with animation.
     */
    public void loadView(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // 1. Set initial opacity to 0 (Invisible)
            root.setOpacity(0);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            // 2. Create Fade Transition (0.0 -> 1.0)
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