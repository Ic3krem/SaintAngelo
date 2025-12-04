package com.stangelo.saintangelo.controllers;

import java.io.IOException;

import com.stangelo.saintangelo.dao.UserDAO;
import com.stangelo.saintangelo.models.User;
import com.stangelo.saintangelo.models.UserRole;
import com.stangelo.saintangelo.services.AuthService;
import com.stangelo.saintangelo.utils.DatabaseConnection;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink publicViewLink;

    private UserDAO userDAO;
    private AuthService authService;

    @FXML
    public void initialize() {
        // Initialize DAO and AuthService
        userDAO = new UserDAO();
        authService = AuthService.getInstance();

        // Test database connection on initialization
        if (!DatabaseConnection.testConnection()) {
            String dbUrl = DatabaseConnection.getDatabaseUrl();
            showAlert("Database Connection Error",
                    "Cannot connect to shared database. Please ensure:\n" +
                            "1. The database server at 192.168.100.25 is running\n" +
                            "2. You are connected to the LAN network\n" +
                            "3. Database 'saintangelo_hospital' exists on the server\n" +
                            "4. Connection settings in database.properties are correct\n" +
                            "   Current URL: " + dbUrl + "\n" +
                            "5. Firewall allows connection to port 3306\n" +
                            "6. Check the console/logs for detailed error messages");
        }
    }

    @FXML
    public void handleLoginButtonAction() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Please enter both username and password.");
            return;
        }

        // Authenticate user from database
        try {
            User authenticatedUser = userDAO.authenticate(username, password);

            if (authenticatedUser != null) {
                // Set current user in session
                authService.setCurrentUser(authenticatedUser);

                // Load appropriate dashboard based on user role
                if (loadDashboardForUser(authenticatedUser)) {
                    // Clear password field for security
                    passwordField.clear();
                } else {
                    authService.logout();
                    showAlert("Error", "Failed to load dashboard. Please try again.");
                }
            } else {
                showAlert("Login Failed", 
                    "Invalid username or password. Please try again.\n\n" +
                    "Note: Check console logs for detailed error information.");
                // Clear password field
                passwordField.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Login Error", 
                "An error occurred during login: " + e.getMessage() + "\n\n" +
                "Please check the console for details.");
            passwordField.clear();
        }
    }

    /**
     * Loads the appropriate dashboard based on user role
     *
     * @param user Authenticated user
     * @return true if dashboard loaded successfully, false otherwise
     */
    private boolean loadDashboardForUser(User user) {
        String fxmlFile = null;
        String dashboardTitle = null;
        UserRole role = user.getRole();

        // Map user role to appropriate dashboard
        switch (role) {
            case STAFF:
                fxmlFile = "/fxml/receptionist-dashboard-view.fxml";
                dashboardTitle = "Receptionist Dashboard - " + user.getFullName();
                break;
            case DOCTOR:
                fxmlFile = "/fxml/doctor-dashboard-view.fxml";
                dashboardTitle = "Doctor Dashboard - " + user.getFullName();
                break;
            case ADMIN:
            case SUPER_ADMIN:
                fxmlFile = "/fxml/admin-dashboard-view.fxml";
                dashboardTitle = "Admin Dashboard - " + user.getFullName();
                break;
            default:
                showAlert("Access Denied", "Your account does not have access to any dashboard.");
                return false;
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

    /**
     * Shows an alert dialog
     *
     * @param title Alert title
     * @param message Alert message
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog
     *
     * @param title Alert title
     * @param message Alert message
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handles the Public View link action to open the public display view
     */
    @FXML
    public void handlePublicViewLinkAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/public view.fxml"));
            Parent publicViewRoot = loader.load();

            // Set initial opacity to 0 for fade animation
            publicViewRoot.setOpacity(0);

            Stage publicViewStage = new Stage();
            publicViewStage.setTitle("Public View - St. Angelo Medical Center");
            publicViewStage.setScene(new Scene(publicViewRoot));

            // Set to full screen
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            publicViewStage.setX(bounds.getMinX());
            publicViewStage.setY(bounds.getMinY());
            publicViewStage.setWidth(bounds.getWidth());
            publicViewStage.setHeight(bounds.getHeight());
            publicViewStage.setMaximized(true);

            publicViewStage.show();

            // Play fade transition
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), publicViewRoot);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load the public view.\nCheck that /fxml/public view.fxml exists in resources.");
        }
    }
}