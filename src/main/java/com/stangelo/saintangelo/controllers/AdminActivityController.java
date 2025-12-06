package com.stangelo.saintangelo.controllers;

import com.stangelo.saintangelo.dao.ActivityLogDAO;
import com.stangelo.saintangelo.models.ActivityLog;
import com.stangelo.saintangelo.models.ActivityType;
import com.stangelo.saintangelo.models.User;
import com.stangelo.saintangelo.models.UserRole;
import com.stangelo.saintangelo.services.AuthService;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.HPos;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminActivityController implements Initializable {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilter;
    @FXML
    private VBox activityRowsContainer;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private Button refreshButton;

    private ActivityLogDAO activityLogDAO;
    private List<ActivityLog> allLogs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activityLogDAO = new ActivityLogDAO();
        updateUserInfo();
        initializeFilters();
        initializeRefreshButton();
        loadActivityLogs();
    }

    private void initializeRefreshButton() {
        // Refresh button handler is set via FXML onAction="#handleRefresh"
    }

    /**
     * Handles refresh button click
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshActivityLogs();
    }

    /**
     * Refreshes activity logs from the database
     */
    public void refreshActivityLogs() {
        loadActivityLogs();
        if (refreshButton != null) {
            // Show visual feedback
            refreshButton.setText("Refreshing...");
            refreshButton.setDisable(true);
            javafx.application.Platform.runLater(() -> {
                refreshButton.setText("Refresh");
                refreshButton.setDisable(false);
            });
        }
    }

    private void initializeFilters() {
        // Populate activity type filter
        typeFilter.getItems().add("All Types");
        for (ActivityType type : ActivityType.values()) {
            typeFilter.getItems().add(type.name());
        }
        typeFilter.setValue("All Types");
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayLogs());

        // Search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayLogs());
        }
    }

    private void loadActivityLogs() {
        try {
            System.out.println("Loading activity logs from database...");
            
            // First check total count in database
            int totalCount = activityLogDAO.getTotalCount();
            System.out.println("Total activity logs in database: " + totalCount);
            
            allLogs = activityLogDAO.findAll();
            if (allLogs == null) {
                allLogs = new java.util.ArrayList<>();
                System.out.println("Warning: findAll() returned null, using empty list");
            }
            
            System.out.println("Loaded " + allLogs.size() + " activity logs from database");
            
            // Warn if count doesn't match
            if (totalCount > 0 && allLogs.isEmpty()) {
                System.out.println("WARNING: Database has " + totalCount + " logs but findAll() returned empty list!");
                showAlert(Alert.AlertType.WARNING, "Data Mismatch", 
                        "Database contains " + totalCount + " activity logs, but none were loaded. " +
                        "Check console for errors.");
            }
            
            // Debug: Print first few logs
            if (!allLogs.isEmpty()) {
                System.out.println("Sample logs (first " + Math.min(5, allLogs.size()) + "):");
                for (int i = 0; i < Math.min(5, allLogs.size()); i++) {
                    ActivityLog log = allLogs.get(i);
                    System.out.println("  Log " + (i+1) + ": ID=" + log.getLogId() + 
                                     ", User=" + (log.getUser() != null ? log.getUser().getFullName() : "System") +
                                     ", Action=" + log.getAction() +
                                     ", Type=" + (log.getActivityType() != null ? log.getActivityType().name() : "null"));
                }
            } else if (totalCount == 0) {
                System.out.println("No activity logs found in database - database is empty");
            }
            
            filterAndDisplayLogs();
        } catch (Exception e) {
            System.err.println("Error loading activity logs: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                    "Failed to load activity logs from database: " + e.getMessage());
            allLogs = new java.util.ArrayList<>();
            filterAndDisplayLogs();
        }
    }

    private void filterAndDisplayLogs() {
        if (allLogs == null) {
            System.out.println("filterAndDisplayLogs: allLogs is null");
            displayActivityLogs(new java.util.ArrayList<>());
            return;
        }

        String searchTerm = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String selectedType = typeFilter != null && typeFilter.getValue() != null 
                ? typeFilter.getValue() : "All Types";

        System.out.println("Filtering logs - Total logs: " + allLogs.size() + 
                          ", Search term: '" + searchTerm + "', Type filter: " + selectedType);

        List<ActivityLog> filteredLogs = allLogs.stream()
                .filter(log -> {
                    if (log == null) {
                        return false;
                    }
                    
                    // Search filter
                    if (!searchTerm.isEmpty()) {
                        boolean matchesSearch = 
                            (log.getAction() != null && log.getAction().toLowerCase().contains(searchTerm)) ||
                            (log.getDetails() != null && log.getDetails().toLowerCase().contains(searchTerm)) ||
                            (log.getUser() != null && log.getUser().getFullName() != null && 
                             log.getUser().getFullName().toLowerCase().contains(searchTerm)) ||
                            (String.valueOf(log.getLogId()).contains(searchTerm));
                        if (!matchesSearch) {
                            return false;
                        }
                    }

                    // Type filter
                    if (!selectedType.equals("All Types")) {
                        if (log.getActivityType() == null || 
                            !log.getActivityType().name().equals(selectedType)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("After filtering: " + filteredLogs.size() + " logs to display");
        displayActivityLogs(filteredLogs);
    }

    private void displayActivityLogs(List<ActivityLog> logs) {
        if (activityRowsContainer == null) {
            System.out.println("displayActivityLogs: activityRowsContainer is null");
            return;
        }

        activityRowsContainer.getChildren().clear();

        if (logs == null || logs.isEmpty()) {
            System.out.println("displayActivityLogs: No logs to display");
            if (emptyStateLabel != null) {
                emptyStateLabel.setVisible(true);
                emptyStateLabel.setManaged(true);
            }
            return;
        }

        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(false);
            emptyStateLabel.setManaged(false);
        }

        System.out.println("Displaying " + logs.size() + " activity logs");
        int displayedCount = 0;
        
        for (int i = 0; i < logs.size(); i++) {
            ActivityLog log = logs.get(i);
            if (log == null) {
                System.out.println("Skipping null log at index " + i);
                continue; // Skip null logs
            }
            try {
                GridPane row = createActivityRow(log);
                activityRowsContainer.getChildren().add(row);
                displayedCount++;
                
                // Add separator between rows (not after the last one)
                if (i < logs.size() - 1) {
                    activityRowsContainer.getChildren().add(new Separator());
                }
            } catch (Exception e) {
                System.err.println("Error displaying activity log at index " + i + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Successfully displayed " + displayedCount + " activity log rows");
    }

    private GridPane createActivityRow(ActivityLog log) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row-item");
        
        row.getColumnConstraints().addAll(
                createColumnConstraint(10.0),
                createColumnConstraint(15.0),
                createColumnConstraint(25.0),
                createColumnConstraint(25.0),
                createColumnConstraint(10.0),
                createColumnConstraint(15.0)
        );

        // Log ID
        Label logIdLabel = new Label(String.valueOf(log.getLogId()));
        logIdLabel.getStyleClass().add("text-cell-bold");
        row.add(logIdLabel, 0, 0);

        // User
        String userName = log.getUser() != null ? log.getUser().getFullName() : "System";
        Label userLabel = new Label(userName);
        userLabel.getStyleClass().add("text-cell");
        row.add(userLabel, 1, 0);

        // Action
        Label actionLabel = new Label(log.getAction() != null ? log.getAction() : "");
        actionLabel.getStyleClass().add("text-cell");
        row.add(actionLabel, 2, 0);

        // Target/Details
        String target = log.getDetails() != null ? log.getDetails() : "";
        if (target.length() > 50) {
            target = target.substring(0, 47) + "...";
        }
        Label targetLabel = new Label(target);
        targetLabel.getStyleClass().add("text-cell");
        row.add(targetLabel, 3, 0);

        // Type
        String typeText = "UNKNOWN";
        if (log.getActivityType() != null) {
            typeText = log.getActivityType().name();
        } else {
            System.out.println("Warning: Activity type is null for log_id: " + log.getLogId());
        }
        Label typeLabel = new Label(typeText);
        typeLabel.getStyleClass().addAll("badge-tag", getActivityTypeStyleClass(log.getActivityType()));
        // Center the label content
        typeLabel.setAlignment(javafx.geometry.Pos.CENTER);
        // Set explicit text color as fallback
        if (log.getActivityType() == null || log.getActivityType() == ActivityType.LOGIN) {
            typeLabel.setStyle("-fx-text-fill: #7b1fa2; -fx-background-color: #f3e5f5;");
        }
        // Center the label in the GridPane cell
        GridPane.setHalignment(typeLabel, HPos.CENTER);
        GridPane.setValignment(typeLabel, javafx.geometry.VPos.CENTER);
        row.add(typeLabel, 4, 0);
        
        // Debug: Print activity type info
        System.out.println("Log ID " + log.getLogId() + " - Activity Type: " + 
                          (log.getActivityType() != null ? log.getActivityType().name() : "NULL") +
                          ", Display text: '" + typeText + "'");

        // Timestamp
        String timestampText = formatTimestamp(log.getTimestamp());
        Label timestampLabel = new Label(timestampText);
        timestampLabel.getStyleClass().add("text-cell");
        row.add(timestampLabel, 5, 0);

        return row;
    }

    private String getActivityTypeStyleClass(ActivityType type) {
        if (type == null) {
            return "tag-system";
        }
        switch (type) {
            case PATIENT:
            case QUEUE:
            case APPOINTMENT:
            case DISCHARGE:
                return "tag-medical";
            case USER_MANAGEMENT:
            case REPORT:
                return "tag-registration";
            case LOGIN:
            default:
                return "tag-system";
        }
    }

    private String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    private ColumnConstraints createColumnConstraint(double percentWidth) {
        ColumnConstraints col = new ColumnConstraints();
        col.setHgrow(Priority.ALWAYS);
        col.setPercentWidth(percentWidth);
        return col;
    }

    @FXML
    private void handleExportLogs(ActionEvent event) {
        if (allLogs == null || allLogs.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No activity logs to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Activity Logs");
        fileChooser.setInitialFileName("activity_logs_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.write("Log ID,User,Action,Details,Type,Timestamp\n");
                
                // Write data
                for (ActivityLog log : allLogs) {
                    writer.write(String.join(",",
                            String.valueOf(log.getLogId()),
                            "\"" + (log.getUser() != null ? log.getUser().getFullName() : "System") + "\"",
                            "\"" + (log.getAction() != null ? log.getAction() : "") + "\"",
                            "\"" + (log.getDetails() != null ? log.getDetails().replace("\"", "\"\"") : "") + "\"",
                            log.getActivityType() != null ? log.getActivityType().name() : "UNKNOWN",
                            formatTimestamp(log.getTimestamp())
                    ));
                    writer.write("\n");
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                        "Activity logs exported successfully to " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                        "Failed to export logs: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        loadView(event, "/fxml/admin-dashboard-view.fxml");
    }

    @FXML
    private void handleNavUsers(ActionEvent event) {
        loadView(event, "/fxml/admin-usermanage-view.fxml");
    }

    @FXML
    private void handleNavActivity(ActionEvent event) {
        loadView(event, "/fxml/admin-activity-view.fxml");
    }

    @FXML
    private void handleNavReports(ActionEvent event) {
        loadView(event, "/fxml/admin-generaterReport-view.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent loginView = loader.load();

            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(loginView);
            scene.setFill(Color.TRANSPARENT);
            loginStage.setScene(scene);
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Try to update user info if the controller supports it
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).updateUserInfo();
            } else if (controller instanceof AdminActivityController) {
                AdminActivityController activityController = (AdminActivityController) controller;
                activityController.updateUserInfo();
                // Refresh logs when navigating to activity view
                activityController.refreshActivityLogs();
            }

            // Set initial opacity to 0 (Invisible)
            root.setOpacity(0);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage == null || stage.getScene() == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not access window.");
                return;
            }
            
            stage.getScene().setRoot(root);

            // Play Fade Transition (0.0 -> 1.0)
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), root);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Could not load " + fxmlPath + "\nCheck if file exists in /fxml/ folder.\nError: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "An error occurred while navigating: " + e.getMessage());
        }
    }

    /**
     * Updates the user name and role labels from the current logged-in user
     */
    public void updateUserInfo() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFullName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(getRoleDisplayName(currentUser.getRole()));
            }
        }
    }

    private String getRoleDisplayName(UserRole role) {
        if (role == null) return "";
        switch (role) {
            case SUPER_ADMIN:
            case ADMIN:
                return "Admin";
            case DOCTOR:
                return "Doctor";
            case STAFF:
                return "Receptionist";
            default:
                return role.name();
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

