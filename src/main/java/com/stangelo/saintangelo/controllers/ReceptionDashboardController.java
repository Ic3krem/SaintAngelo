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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ReceptionDashboardController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private Button btnTabNew;
    @FXML private Button btnTabExisting;
    @FXML private VBox formNewPatient;
    @FXML private VBox formExistingPatient;

    // Discharge Modal
    @FXML private StackPane modalOverlay;
    private Button currentProcessButton; // To track which button opened the modal

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization logic
    }

    // --- DISCHARGE MODAL HANDLERS ---

    @FXML
    private void handleProcess(ActionEvent event) {
        // Store reference to the button that was clicked
        currentProcessButton = (Button) event.getSource();

        // Show the modal
        if (modalOverlay != null) {
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);

            // Optional: Simple fade in
            FadeTransition ft = new FadeTransition(Duration.millis(200), modalOverlay);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }
    }

    @FXML
    private void handleCloseModal(ActionEvent event) {
        closeModal();
    }

    @FXML
    private void handleReviewed(ActionEvent event) {
        // Close the modal
        closeModal();

        // Update the button if it exists
        if (currentProcessButton != null) {
            currentProcessButton.setText("Discharge");

            // Remove old styling classes
            currentProcessButton.getStyleClass().remove("btn-action-process");

            // Add new styling class
            currentProcessButton.getStyleClass().add("btn-action-discharge");

            // Update the handler to perform the actual discharge (or disable it)
            currentProcessButton.setOnAction(e -> {
                System.out.println("Discharging patient...");
                currentProcessButton.setDisable(true);
                currentProcessButton.setText("Discharged");
            });
        }
    }

    private void closeModal() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    // --- REGISTRATION TAB HANDLERS ---

    @FXML
    private void handleTabNewPatient(ActionEvent event) {
        if(btnTabNew != null && formNewPatient != null) {
            setTabActive(btnTabNew, formNewPatient);
            setTabInactive(btnTabExisting, formExistingPatient);
        }
    }

    @FXML
    private void handleTabExistingPatient(ActionEvent event) {
        if(btnTabExisting != null && formExistingPatient != null) {
            setTabActive(btnTabExisting, formExistingPatient);
            setTabInactive(btnTabNew, formNewPatient);
        }
    }

    private void setTabActive(Button btn, VBox form) {
        btn.getStyleClass().removeAll("reg-tab-inactive");
        btn.getStyleClass().add("reg-tab-active");
        if (form != null) {
            form.setVisible(true);
            form.setManaged(true);
        }
    }

    private void setTabInactive(Button btn, VBox form) {
        btn.getStyleClass().removeAll("reg-tab-active");
        btn.getStyleClass().add("reg-tab-inactive");
        if (form != null) {
            form.setVisible(false);
            form.setManaged(false);
        }
    }

    // --- QUEUE GENERATION HANDLER ---

    @FXML
    private void handleGetQueueNumber(ActionEvent event) {
        loadView(event, "/fxml/reception-ticket-view.fxml");
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
        // UPDATED: Navigate to Appointments screen
        loadView(event, "/fxml/receptionist-appointments-view.fxml");
    }

    @FXML
    private void handleNavDischarge(ActionEvent event) {
        // UPDATED: Navigate to Discharge screen
        loadView(event, "/fxml/receptionist-discharge-view.fxml");
    }

    // --- LOGOUT HANDLER ---

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // 1. Close current dashboard
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // 2. Load Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent loginView = loader.load();

            // 3. Create New Transparent Stage
            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            // 4. Reconstruct Title Bar
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

            // Dragging Logic
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

            // 5. Wrap Login View
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: transparent;");
            root.setTop(titleBar);
            root.setCenter(loginView);

            // 6. Set Scene
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());

            loginStage.setScene(scene);
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