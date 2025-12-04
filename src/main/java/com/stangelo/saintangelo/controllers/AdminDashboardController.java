package com.stangelo.saintangelo.controllers;

import com.stangelo.saintangelo.dao.ActivityLogDAO;
import com.stangelo.saintangelo.dao.PatientDAO;
import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.dao.UserDAO;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AdminDashboardController implements Initializable {

    // --- DASHBOARD STAT TILES ---
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label avgWaitTimeLabel;

    // --- DASHBOARD CHARTS & ACTIVITY ---
    @FXML
    private HBox patientFlowChart;
    @FXML
    private VBox systemUsageContainer;
    @FXML
    private VBox recentActivityContainer;

    // User Management FXML Fields
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterComboBox;
    @FXML
    private VBox userTableContainer;
    @FXML
    private Button addUserButton;

    // User Profile Fields (present in all admin views)
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    // DAOs
    private UserDAO userDAO;
    private PatientDAO patientDAO;
    private TicketDAO ticketDAO;
    private ActivityLogDAO activityLogDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Update user info display
        updateUserInfo();

        // Determine which admin view we are on by checking which FXML fields are present
        if (totalUsersLabel != null) {
            // Dashboard view
            userDAO = new UserDAO();
            patientDAO = new PatientDAO();
            ticketDAO = new TicketDAO();
            activityLogDAO = new ActivityLogDAO();
            initializeDashboard();
        }

        if (userTableContainer != null) {
            // User management view
            userDAO = new UserDAO();
            initializeUserManagement();
        }
    }

    // --- DASHBOARD INITIALIZATION ---
    private void initializeDashboard() {
        loadTopTiles();
        populatePatientFlowChart();
        populateSystemUsageStats();
        populateRecentActivity();
    }

    private void loadTopTiles() {
        if (totalUsersLabel != null && userDAO != null) {
            totalUsersLabel.setText(String.valueOf(userDAO.countAllUsers()));
        }
        if (totalPatientsLabel != null && patientDAO != null) {
            totalPatientsLabel.setText(String.valueOf(patientDAO.countAllPatients()));
        }
        if (avgWaitTimeLabel != null && ticketDAO != null) {
            int avgWait = ticketDAO.getAverageWaitTimeToday();
            avgWaitTimeLabel.setText(avgWait + " min");
        }
    }

    private void populatePatientFlowChart() {
        if (patientFlowChart == null || ticketDAO == null) return;

        patientFlowChart.getChildren().clear();
        Map<LocalDate, Integer> counts = ticketDAO.getDailyTicketCountsLast7Days();

        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        int safeMax = Math.max(max, 1);

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            int count = counts.getOrDefault(date, 0);

            VBox column = new VBox(5);
            column.setAlignment(Pos.BOTTOM_CENTER);

            Region bar = new Region();
            bar.getStyleClass().add("chart-bar");
            double heightRatio = (double) count / safeMax;
            bar.setPrefWidth(30);
            bar.setPrefHeight(20 + heightRatio * 110);

            Label value = new Label(String.valueOf(count));
            value.getStyleClass().add("chart-label");

            Label label = new Label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            label.getStyleClass().add("chart-label");

            column.getChildren().addAll(bar, value, label);
            patientFlowChart.getChildren().add(column);
        }
    }

    private void populateSystemUsageStats() {
        if (systemUsageContainer == null || userDAO == null) return;

        systemUsageContainer.getChildren().clear();
        int doctors = userDAO.countByRole(UserRole.DOCTOR);
        int admins = userDAO.countByRole(UserRole.ADMIN) + userDAO.countByRole(UserRole.SUPER_ADMIN);
        int reception = userDAO.countByRole(UserRole.STAFF);

        int total = doctors + admins + reception;
        if (total == 0) {
            Label placeholder = new Label("No user statistics available.");
            placeholder.getStyleClass().add("stat-footer");
            systemUsageContainer.getChildren().add(placeholder);
            return;
        }

        addUsageRow("Doctors", doctors, total, "fill-blue");
        addUsageRow("Receptionists", reception, total, "fill-green");
        addUsageRow("Admins", admins, total, "fill-purple");
    }

    private void addUsageRow(String labelText, int count, int total, String colorClass) {
        double percentage = total == 0 ? 0 : (count * 100.0 / total);

        VBox wrapper = new VBox(5);
        wrapper.getStyleClass().add("usage-row");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("usage-label");

        Label percent = new Label(String.format("%.0f%%", percentage));
        percent.getStyleClass().add("usage-percent");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(label, spacer, percent);

        StackPane track = new StackPane();
        track.getStyleClass().add("progress-track");
        track.setPrefWidth(220);

        Region fill = new Region();
        fill.getStyleClass().addAll("progress-fill", colorClass);
        track.getChildren().add(fill);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);
        fill.prefWidthProperty().bind(track.widthProperty().multiply(percentage / 100));

        wrapper.getChildren().addAll(header, track);
        systemUsageContainer.getChildren().add(wrapper);
    }

    private void populateRecentActivity() {
        if (recentActivityContainer == null || activityLogDAO == null) return;

        recentActivityContainer.getChildren().clear();
        List<ActivityLog> logs = activityLogDAO.findRecent(3);

        if (logs.isEmpty()) {
            Label placeholder = new Label("No activity recorded yet.");
            placeholder.getStyleClass().add("stat-footer");
            recentActivityContainer.getChildren().add(placeholder);
            return;
        }

        for (int i = 0; i < logs.size(); i++) {
            ActivityLog log = logs.get(i);
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("activity-item");

            Label badge = new Label(mapActivityBadgeText(log.getActivityType()));
            badge.getStyleClass().addAll("badge-tag", mapActivityBadgeStyle(log.getActivityType()));

            Label description = new Label(buildActivityDescription(log));
            description.getStyleClass().add("activity-text");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(badge, description, spacer);
            recentActivityContainer.getChildren().add(row);

            if (i < logs.size() - 1) {
                recentActivityContainer.getChildren().add(new Separator());
            }
        }
    }

    private String buildActivityDescription(ActivityLog log) {
        StringBuilder builder = new StringBuilder();
        if (log.getUser() != null) {
            builder.append(log.getUser().getFullName()).append(" - ");
        }
        if (log.getDetails() != null && !log.getDetails().trim().isEmpty()) {
            builder.append(log.getDetails().trim());
        } else {
            builder.append(log.getAction());
        }
        return builder.toString();
    }

    private String mapActivityBadgeText(ActivityType type) {
        if (type == null) {
            return "system";
        }
        switch (type) {
            case PATIENT:
            case QUEUE:
            case APPOINTMENT:
            case DISCHARGE:
                return "medical";
            case USER_MANAGEMENT:
            case REPORT:
                return "admin";
            case LOGIN:
            default:
                return "system";
        }
    }

    private String mapActivityBadgeStyle(ActivityType type) {
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

    // --- USER MANAGEMENT VIEW ---
    /**
     * Initializes the user management view
     */
    private void initializeUserManagement() {
        // Initialize status filter combo box
        if (statusFilterComboBox != null) {
            statusFilterComboBox.getItems().addAll("All Status", "Active", "Inactive");
            statusFilterComboBox.setValue("All Status");
            statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                loadUsers();
            });
        }

        // Setup search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                loadUsers();
            });
        }

        // Load initial users
        loadUsers();
    }

    /**
     * Loads users from database and populates the table
     */
    private void loadUsers() {
        if (userTableContainer == null) return;

        // Clear existing rows (except header)
        userTableContainer.getChildren().clear();
        addTableHeader();

        // Get search term and status filter
        String searchTerm = searchField != null ? searchField.getText().trim() : "";
        String statusFilter = statusFilterComboBox != null && statusFilterComboBox.getValue() != null
                ? statusFilterComboBox.getValue() : "All Status";

        // Get users from database
        List<User> users = userDAO.searchWithFilters(
                searchTerm.isEmpty() ? null : searchTerm,
                statusFilter.equals("All Status") ? null : statusFilter
        );

        // Populate table with users
        for (User user : users) {
            addUserRow(user);
        }
    }

    /**
     * Adds the table header row
     */
    private void addTableHeader() {
        GridPane header = new GridPane();
        header.getStyleClass().add("table-header-green");
        
        // Column constraints
        header.getColumnConstraints().addAll(
                createColumnConstraint(10.0),
                createColumnConstraint(14.0),
                createColumnConstraint(11.0),
                createColumnConstraint(20.0),
                createColumnConstraint(16.0),
                createColumnConstraint(9.0),
                createColumnConstraint(10.0),
                createColumnConstraint(10.0)
        );

        // Header labels
        header.add(createHeaderLabel("User ID"), 0, 0);
        header.add(createHeaderLabel("Name"), 1, 0);
        header.add(createHeaderLabel("Role"), 2, 0);
        header.add(createHeaderLabel("Email"), 3, 0);
        header.add(createHeaderLabel("Permissions"), 4, 0);
        header.add(createHeaderLabel("Status"), 5, 0);
        header.add(createHeaderLabel("Last Active"), 6, 0);
        header.add(createHeaderLabel("Actions"), 7, 0);

        userTableContainer.getChildren().add(header);
    }

    private javafx.scene.control.Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("table-col-header");
        return label;
    }

    private javafx.scene.layout.ColumnConstraints createColumnConstraint(double percentWidth) {
        javafx.scene.layout.ColumnConstraints col = new javafx.scene.layout.ColumnConstraints();
        col.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col.setPercentWidth(percentWidth);
        return col;
    }

    /**
     * Adds a user row to the table
     */
    private void addUserRow(User user) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row-item");

        // Column constraints
        row.getColumnConstraints().addAll(
                createColumnConstraint(10.0),
                createColumnConstraint(13.0),
                createColumnConstraint(12.0),
                createColumnConstraint(20.0),
                createColumnConstraint(15.0),
                createColumnConstraint(10.0),
                createColumnConstraint(10.0),
                createColumnConstraint(10.0)
        );

        // User ID
        Label userIdLabel = new Label(user.getId());
        userIdLabel.getStyleClass().add("text-cell-bold");
        row.add(userIdLabel, 0, 0);

        // Name
        Label nameLabel = new Label(user.getFullName());
        nameLabel.getStyleClass().add("text-cell");
        row.add(nameLabel, 1, 0);

        // Role
        Label roleLabel = new Label(getRoleDisplayName(user.getRole()));
        roleLabel.getStyleClass().addAll("badge-role", getRoleStyleClass(user.getRole()));
        row.add(roleLabel, 2, 0);

        // Email
        Label emailLabel = new Label(user.getEmail() != null ? user.getEmail() : "");
        emailLabel.getStyleClass().add("text-cell");
        row.add(emailLabel, 3, 0);

        // Permissions
        Label permissionsLabel = new Label(user.getPermissions() != null ? user.getPermissions() : "");
        permissionsLabel.getStyleClass().add("text-cell");
        row.add(permissionsLabel, 4, 0);

        // Status
        Label statusLabel = new Label(user.getStatus() != null ? user.getStatus() : "Active");
        statusLabel.getStyleClass().add(user.getStatus() != null && user.getStatus().equals("Active") 
                ? "status-active" : "status-inactive");
        row.add(statusLabel, 5, 0);

        // Last Active
        Label lastActiveLabel = new Label(formatLastActive(user.getLastActive()));
        lastActiveLabel.getStyleClass().add("text-cell");
        row.add(lastActiveLabel, 6, 0);

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        // Edit button
        Button editButton = new Button();
        editButton.getStyleClass().add("btn-action-icon");
        SVGPath editIcon = new SVGPath();
        editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        editIcon.setFill(javafx.scene.paint.Color.valueOf("#0b7d56"));
        editIcon.setScaleX(0.7);
        editIcon.setScaleY(0.7);
        editButton.setGraphic(editIcon);
        editButton.setOnAction(e -> handleEditUser(user));

        // View button
        Button viewButton = new Button();
        viewButton.getStyleClass().add("btn-action-icon");
        SVGPath viewIcon = new SVGPath();
        viewIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        viewIcon.setFill(javafx.scene.paint.Color.valueOf("#0b7d56"));
        viewIcon.setScaleX(0.7);
        viewIcon.setScaleY(0.7);
        viewButton.setGraphic(viewIcon);
        viewButton.setOnAction(e -> handleViewUser(user));

        // Delete button
        Button deleteButton = new Button();
        deleteButton.getStyleClass().add("btn-action-icon");
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        deleteIcon.setFill(javafx.scene.paint.Color.valueOf("#d9534f"));
        deleteIcon.setScaleX(0.7);
        deleteIcon.setScaleY(0.7);
        deleteButton.setGraphic(deleteIcon);
        deleteButton.setOnAction(e -> handleDeleteUser(user));

        actionsBox.getChildren().addAll(editButton, viewButton, deleteButton);
        row.add(actionsBox, 7, 0);

        userTableContainer.getChildren().add(row);
    }

    /**
     * Gets the display name for a role
     */
    private String getRoleDisplayName(UserRole role) {
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

    /**
     * Gets the CSS style class for a role
     */
    private String getRoleStyleClass(UserRole role) {
        switch (role) {
            case SUPER_ADMIN:
            case ADMIN:
                return "role-admin";
            case DOCTOR:
                return "role-doctor";
            case STAFF:
                return "role-receptionist";
            default:
                return "";
        }
    }

    /**
     * Formats the last active timestamp
     */
    private String formatLastActive(LocalDateTime lastActive) {
        if (lastActive == null) {
            return "Never";
        }

        long minutesAgo = ChronoUnit.MINUTES.between(lastActive, LocalDateTime.now());
        if (minutesAgo < 1) {
            return "Now";
        } else if (minutesAgo < 60) {
            return minutesAgo + " min ago";
        } else {
            long hoursAgo = minutesAgo / 60;
            if (hoursAgo < 24) {
                return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
            } else {
                long daysAgo = hoursAgo / 24;
                return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
            }
        }
    }

    /**
     * Handles edit user action
     */
    private void handleEditUser(User user) {
        showUserEditDialog(user);
    }

    /**
     * Handles view user action
     */
    private void handleViewUser(User user) {
        showUserViewDialog(user);
    }

    /**
     * Handles delete user action
     */
    private void handleDeleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText("Confirm Deletion");
        confirmAlert.setContentText("Are you sure you want to delete user " + user.getFullName() + " (" + user.getId() + ")?\n\nThis action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userDAO.delete(user.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    loadUsers(); // Refresh table
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user. Please try again.");
                }
            }
        });
    }

    /**
     * Shows user edit dialog
     */
    private void showUserEditDialog(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit User Information");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField(user.getUsername());
        TextField fullNameField = new TextField(user.getFullName());
        TextField emailField = new TextField(user.getEmail() != null ? user.getEmail() : "");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Leave blank to keep current password");
        ComboBox<UserRole> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setValue(user.getRole());
        TextField permissionsField = new TextField(user.getPermissions() != null ? user.getPermissions() : "");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Active", "Inactive");
        statusComboBox.setValue(user.getStatus() != null ? user.getStatus() : "Active");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);
        grid.add(new Label("Permissions:"), 0, 5);
        grid.add(permissionsField, 1, 5);
        grid.add(new Label("Status:"), 0, 6);
        grid.add(statusComboBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUsername(usernameField.getText());
                user.setFullName(fullNameField.getText());
                user.setEmail(emailField.getText());
                // Only update password if a new one was entered
                String newPassword = passwordField.getText().trim();
                if (!newPassword.isEmpty()) {
                    user.setPassword(newPassword);
                }
                user.setRole(roleComboBox.getValue());
                user.setPermissions(permissionsField.getText());
                user.setStatus(statusComboBox.getValue());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            if (userDAO.update(updatedUser, updatedUser.getEmail(), updatedUser.getPermissions(), updatedUser.getStatus())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");
                loadUsers(); // Refresh table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user. Please try again.");
            }
        });
    }

    /**
     * Shows user view dialog
     */
    private void showUserViewDialog(User user) {
        Alert viewAlert = new Alert(Alert.AlertType.INFORMATION);
        viewAlert.setTitle("User Details");
        viewAlert.setHeaderText("User Information");

        StringBuilder content = new StringBuilder();
        content.append("User ID: ").append(user.getId()).append("\n");
        content.append("Username: ").append(user.getUsername()).append("\n");
        content.append("Full Name: ").append(user.getFullName()).append("\n");
        content.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "N/A").append("\n");
        content.append("Role: ").append(getRoleDisplayName(user.getRole())).append("\n");
        content.append("Permissions: ").append(user.getPermissions() != null ? user.getPermissions() : "N/A").append("\n");
        content.append("Status: ").append(user.getStatus() != null ? user.getStatus() : "Active").append("\n");
        content.append("Last Active: ").append(formatLastActive(user.getLastActive()));

        viewAlert.setContentText(content.toString());
        viewAlert.showAndWait();
    }

    /**
     * Handles add new user action
     */
    @FXML
    public void handleAddUser(ActionEvent event) {
        showAddUserDialog();
    }

    /**
     * Shows add new user dialog
     */
    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create New User Account");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField userIdField = new TextField();
        userIdField.setText(generateNextUserId());
        userIdField.setEditable(false);
        userIdField.setStyle("-fx-background-color: #f0f0f0;");
        TextField usernameField = new TextField();
        TextField fullNameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<UserRole> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(UserRole.values());
        roleComboBox.setValue(UserRole.STAFF);
        TextField permissionsField = new TextField();
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Active", "Inactive");
        statusComboBox.setValue("Active");

        // Set default permissions based on role
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case DOCTOR:
                        permissionsField.setText("Full Medical Access");
                        break;
                    case STAFF:
                        permissionsField.setText("Registration & Queue");
                        break;
                    case ADMIN:
                    case SUPER_ADMIN:
                        permissionsField.setText("System Configuration");
                        break;
                }
            }
        });

        grid.add(new Label("User ID:"), 0, 0);
        grid.add(userIdField, 1, 0);
        grid.add(new Label("Username:*"), 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(new Label("Full Name:*"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Email:*"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Password:*"), 0, 4);
        grid.add(passwordField, 1, 4);
        grid.add(new Label("Role:*"), 0, 5);
        grid.add(roleComboBox, 1, 5);
        grid.add(new Label("Permissions:"), 0, 6);
        grid.add(permissionsField, 1, 6);
        grid.add(new Label("Status:"), 0, 7);
        grid.add(statusComboBox, 1, 7);

        Label requiredLabel = new Label("* Required fields");
        requiredLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        grid.add(requiredLabel, 1, 8);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Create User", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Enable/disable save button based on required fields
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Validate required fields
        Runnable validateFields = () -> {
            boolean isValid = !usernameField.getText().trim().isEmpty() &&
                    !fullNameField.getText().trim().isEmpty() &&
                    !emailField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty() &&
                    roleComboBox.getValue() != null;
            saveButton.setDisable(!isValid);
        };

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String userId = userIdField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String fullName = fullNameField.getText().trim();
                String email = emailField.getText().trim();
                UserRole role = roleComboBox.getValue();
                String permissions = permissionsField.getText().trim();
                String status = statusComboBox.getValue();

                User newUser = new User(userId, username, password, fullName, role);
                newUser.setEmail(email);
                newUser.setPermissions(permissions);
                newUser.setStatus(status);

                return newUser;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            // Check if username already exists
            if (userDAO.findByUsername(newUser.getUsername()) != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Username already exists. Please choose a different username.");
                return;
            }

            // Check if email already exists
            if (userDAO.findByEmail(newUser.getEmail()) != null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Email already exists. Please use a different email.");
                return;
            }

            if (userDAO.create(newUser, newUser.getEmail(), newUser.getPermissions())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User created successfully.");
                loadUsers(); // Refresh table
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create user. Please try again.");
            }
        });
    }

    /**
     * Generates the next available user ID
     */
    private String generateNextUserId() {
        List<User> allUsers = userDAO.findAll();
        int maxId = 0;
        
        for (User user : allUsers) {
            String userId = user.getId();
            if (userId != null && userId.startsWith("U")) {
                try {
                    int idNum = Integer.parseInt(userId.substring(1));
                    if (idNum > maxId) {
                        maxId = idNum;
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }
        
        return String.format("U%03d", maxId + 1);
    }

    // --- NAVIGATION HANDLERS ---

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
        loadView(event, "/fxml/admin-generaterReport-view.fxml.");
    }


    // --- LOGOUT HANDLER (Consistent with MedicalDashboard) ---

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // 1. Close the current dashboard stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // 2. Load the Login View
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Parent loginView = loader.load();

            // 3. Create a NEW Stage for Login (Critical for StageStyle.TRANSPARENT)
            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.TRANSPARENT);

            // 4. Reconstruct the Custom Title Bar
            HBox titleBar = new HBox();
            titleBar.setAlignment(Pos.CENTER_LEFT);
            titleBar.setPadding(new Insets(10, 5, 5, 10));
            titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 0;");

            Label titleLabel = new Label("Saint Angelo Medical Center");
            titleLabel.setTextFill(Color.WHITE);

            Pane spacer = new Pane();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox controlButtons = new HBox(10);
            controlButtons.setAlignment(Pos.CENTER);

            // Control Buttons
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

            // 5. Wrap login view
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: transparent;");
            root.setTop(titleBar);
            root.setCenter(loginView);

            // 6. Create Scene
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());

            // 7. Show Login Stage
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // --- HELPER METHODS ---

    /**
     * Switches the current scene's root to a new FXML view with a fade animation.
     */
    private void loadView(ActionEvent event, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Get the controller instance to update user info
            AdminDashboardController controller = loader.getController();
            if (controller != null) {
                controller.updateUserInfo();
            }

            // 1. Set initial opacity to 0 (Invisible)
            root.setOpacity(0);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

            // 2. Play Fade Transition (0.0 -> 1.0)
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), root);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load " + fxmlPath + "\nCheck if file exists in /fxml/ folder.");
        }
    }

    /**
     * Updates the user name and role labels from the current logged-in user
     */
    private void updateUserInfo() {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}