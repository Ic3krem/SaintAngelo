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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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

    // Dashboard Stats FXML Fields
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label avgWaitTimeLabel;
    @FXML
    private Label totalUsersChangeLabel;
    @FXML
    private Label totalPatientsChangeLabel;
    @FXML
    private Label avgWaitTimeChangeLabel;
    @FXML
    private SVGPath totalUsersArrow;
    @FXML
    private SVGPath totalPatientsArrow;
    @FXML
    private SVGPath avgWaitTimeArrow;
    @FXML
    private LineChart<Number, Number> totalUsersChart;
    @FXML
    private LineChart<Number, Number> totalPatientsChart;
    @FXML
    private LineChart<Number, Number> avgWaitTimeChart;

    // Report Generation FXML Fields
    @FXML
    private ComboBox<String> reportTypeComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ComboBox<String> exportFormatComboBox;
    @FXML
    private Button generateReportButton;


    private UserDAO userDAO;
    private PatientDAO patientDAO;
    private TicketDAO ticketDAO;
    // DAOs
    private UserDAO userDAO;
    private PatientDAO patientDAO;
    private TicketDAO ticketDAO;
    private ActivityLogDAO activityLogDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        patientDAO = new PatientDAO();
        ticketDAO = new TicketDAO();

        updateUserInfo();

        if (userTableContainer != null) {
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

        if (totalUsersChart != null) {
            initializeDashboardCharts();
        }

        if (reportTypeComboBox != null) {
            initializeReportGenerator();
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
        if (statusFilterComboBox != null) {
            statusFilterComboBox.getItems().addAll("All Status", "Active", "Inactive");
            statusFilterComboBox.setValue("All Status");
            statusFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadUsers());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> loadUsers());
        }

        loadUsers();
    }

    private void initializeDashboardCharts() {
        LocalDate today = LocalDate.now();
        int daysToFetch = 7;

        // Total Users
        Map<LocalDate, Integer> userCounts = userDAO.getDailyUserCounts(daysToFetch);
        int currentUsers = userDAO.findAll().size();
        int previousUsers = userDAO.getUserCountInPeriod(today.minusDays(daysToFetch * 2), today.minusDays(daysToFetch));
        updateStatCard(totalUsersLabel, totalUsersChangeLabel, totalUsersArrow, currentUsers, previousUsers, "Increased", "Decreased", "#0b7d56", "#ff6b6b");
        populateChart(totalUsersChart, userCounts, "#0b7d56");

        // Total Patients
        Map<LocalDate, Integer> patientCounts = patientDAO.getDailyPatientCounts(daysToFetch);
        int currentPatients = patientDAO.findAll().size();
        int previousPatients = patientDAO.getPatientCountInPeriod(today.minusDays(daysToFetch * 2), today.minusDays(daysToFetch));
        updateStatCard(totalPatientsLabel, totalPatientsChangeLabel, totalPatientsArrow, currentPatients, previousPatients, "Increased", "Decreased", "#76ff03", "#ff6b6b");
        populateChart(totalPatientsChart, patientCounts, "#76ff03");

        // Avg. Wait Time
        Map<LocalDate, Integer> avgWaitTimes = ticketDAO.getDailyAverageWaitTimesLast7Days();
        int currentAvgWaitTime = ticketDAO.getAverageWaitTimeToday();
        double previousAvgWaitTime = ticketDAO.getAverageWaitTimePrevious7Days();
        updateStatCard(avgWaitTimeLabel, avgWaitTimeChangeLabel, avgWaitTimeArrow, currentAvgWaitTime, (int) previousAvgWaitTime, "Decreased", "Increased", "#ff6b6b", "#0b7d56");
        populateChart(avgWaitTimeChart, avgWaitTimes, "#64ffda");
    }

    private void initializeReportGenerator() {
        reportTypeComboBox.getItems().addAll("Patient Report");
        reportTypeComboBox.setValue("Patient Report");
        exportFormatComboBox.getItems().addAll("CSV");
        exportFormatComboBox.setValue("CSV");
    }

    @FXML
    private void handleGenerateReport(ActionEvent event) {
        String reportType = reportTypeComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String exportFormat = exportFormatComboBox.getValue();

        if (reportType == null || startDate == null || endDate == null || exportFormat == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields.");
            return;
        }

        if (startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Start date cannot be after end date.");
            return;
        }

        String csvData = "";
        if ("Patient Report".equals(reportType)) {
            csvData = generatePatientReport(startDate, endDate);
        }

        if (csvData.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Data", "No data found for the selected criteria.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(reportType.replace(" ", "_") + "_" + startDate + "_to_" + endDate + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(csvData);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report generated successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to save report: " + e.getMessage());
            }
        }
    }

    private String generatePatientReport(LocalDate startDate, LocalDate endDate) {
        List<com.stangelo.saintangelo.models.Patient> patients = patientDAO.findAll().stream()
                .filter(p -> p.getRegistrationDate() != null && !p.getRegistrationDate().isBefore(startDate) && !p.getRegistrationDate().isAfter(endDate))
                .collect(Collectors.toList());

        if (patients.isEmpty()) {
            return "";
        }

        StringBuilder csv = new StringBuilder("Patient ID,Name,Age,Gender,Phone Number,Registration Date\n");
        for (com.stangelo.saintangelo.models.Patient patient : patients) {
            csv.append(String.join(",",
                    patient.getId(),
                    "\"" + patient.getName() + "\"",
                    String.valueOf(patient.getAge()),
                    patient.getGender(),
                    patient.getContactNumber(),
                    patient.getRegistrationDate().toString()
            )).append("\n");
        }
        return csv.toString();
    }


    private void populateChart(LineChart<Number, Number> chart, Map<LocalDate, Integer> data, String color) {
        if (chart == null || data == null || data.isEmpty()) {
            return;
        }

        chart.getData().clear();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        List<LocalDate> sortedDates = new ArrayList<>(data.keySet());
        Collections.sort(sortedDates);

        for (int i = 0; i < sortedDates.size(); i++) {
            LocalDate date = sortedDates.get(i);
            series.getData().add(new XYChart.Data<>(i, data.get(date)));
        }

        chart.getData().add(series);
        chart.lookup(".chart-series-line").setStyle("-fx-stroke: " + color + ";");
    }

    private void updateStatCard(Label valueLabel, Label changeLabel, SVGPath arrowPath,
                                int currentValue, int previousValue,
                                String positiveChangeText, String negativeChangeText,
                                String positiveColor, String negativeColor) {
        if (valueLabel != null) {
            valueLabel.setText(String.valueOf(currentValue));
        }

        if (changeLabel != null && arrowPath != null) {
            double change = currentValue - previousValue;
            double percentageChange = (previousValue != 0) ? (change / previousValue) * 100 : (currentValue > 0 ? 100 : 0);

            String arrowContent;
            String changeText;
            Color arrowColor;

            if (change > 0) {
                arrowContent = "M7 14l5-5 5 5z"; // Up arrow
                changeText = String.format("+%.0f%% %s vs Last Month", Math.abs(percentageChange), positiveChangeText);
                arrowColor = Color.valueOf(positiveColor);
            } else if (change < 0) {
                arrowContent = "M7 10l5 5 5-5z"; // Down arrow
                changeText = String.format("-%.0f%% %s vs Last Month", Math.abs(percentageChange), negativeChangeText);
                arrowColor = Color.valueOf(negativeColor);
            } else {
                arrowContent = ""; // No arrow
                changeText = "No change vs Last Month";
                arrowColor = Color.GRAY;
            }

            arrowPath.setContent(arrowContent);
            arrowPath.setFill(arrowColor);
            changeLabel.setText(changeText);
        }
    }

    private void loadUsers() {
        if (userTableContainer == null) return;

        userTableContainer.getChildren().clear();
        addTableHeader();

        String searchTerm = searchField != null ? searchField.getText().trim() : "";
        String statusFilter = statusFilterComboBox != null && statusFilterComboBox.getValue() != null
                ? statusFilterComboBox.getValue() : "All Status";

        List<User> users = userDAO.searchWithFilters(
                searchTerm.isEmpty() ? null : searchTerm,
                statusFilter.equals("All Status") ? null : statusFilter
        );

        for (User user : users) {
            addUserRow(user);
        }
    }

    private void addTableHeader() {
        GridPane header = new GridPane();
        header.getStyleClass().add("table-header-green");
        
        header.getColumnConstraints().addAll(
                createColumnConstraint(10.0), createColumnConstraint(14.0),
                createColumnConstraint(11.0), createColumnConstraint(20.0),
                createColumnConstraint(16.0), createColumnConstraint(9.0),
                createColumnConstraint(10.0), createColumnConstraint(10.0)
        );

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

    private Label createHeaderLabel(String text) {
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

    private void addUserRow(User user) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-row-item");

        row.getColumnConstraints().addAll(
                createColumnConstraint(10.0), createColumnConstraint(13.0),
                createColumnConstraint(12.0), createColumnConstraint(20.0),
                createColumnConstraint(15.0), createColumnConstraint(10.0),
                createColumnConstraint(10.0), createColumnConstraint(10.0)
        );

        Label userIdLabel = new Label(user.getId());
        userIdLabel.getStyleClass().add("text-cell-bold");
        row.add(userIdLabel, 0, 0);

        Label nameLabel = new Label(user.getFullName());
        nameLabel.getStyleClass().add("text-cell");
        row.add(nameLabel, 1, 0);

        Label roleLabel = new Label(getRoleDisplayName(user.getRole()));
        roleLabel.getStyleClass().addAll("badge-role", getRoleStyleClass(user.getRole()));
        row.add(roleLabel, 2, 0);

        Label emailLabel = new Label(user.getEmail() != null ? user.getEmail() : "");
        emailLabel.getStyleClass().add("text-cell");
        row.add(emailLabel, 3, 0);

        Label permissionsLabel = new Label(user.getPermissions() != null ? user.getPermissions() : "");
        permissionsLabel.getStyleClass().add("text-cell");
        row.add(permissionsLabel, 4, 0);

        Label statusLabel = new Label(user.getStatus() != null ? user.getStatus() : "Active");
        statusLabel.getStyleClass().add(user.getStatus() != null && user.getStatus().equals("Active") 
                ? "status-active" : "status-inactive");
        row.add(statusLabel, 5, 0);

        Label lastActiveLabel = new Label(formatLastActive(user.getLastActive()));
        lastActiveLabel.getStyleClass().add("text-cell");
        row.add(lastActiveLabel, 6, 0);

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);

        Button editButton = createActionButton("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z", "#0b7d56", e -> handleEditUser(user));
        Button viewButton = createActionButton("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z", "#0b7d56", e -> handleViewUser(user));
        Button deleteButton = createActionButton("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z", "#d9534f", e -> handleDeleteUser(user));

        actionsBox.getChildren().addAll(editButton, viewButton, deleteButton);
        row.add(actionsBox, 7, 0);

        userTableContainer.getChildren().add(row);
    }

    private Button createActionButton(String svgContent, String color, javafx.event.EventHandler<ActionEvent> handler) {
        Button button = new Button();
        button.getStyleClass().add("btn-action-icon");
        SVGPath icon = new SVGPath();
        icon.setContent(svgContent);
        icon.setFill(Color.valueOf(color));
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        button.setGraphic(icon);
        button.setOnAction(handler);
        return button;
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

    private String getRoleStyleClass(UserRole role) {
        if (role == null) return "";
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

    private String formatLastActive(LocalDateTime lastActive) {
        if (lastActive == null) return "Never";
        long minutesAgo = ChronoUnit.MINUTES.between(lastActive, LocalDateTime.now());
        if (minutesAgo < 1) return "Now";
        if (minutesAgo < 60) return minutesAgo + " min ago";
        long hoursAgo = minutesAgo / 60;
        if (hoursAgo < 24) return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
        long daysAgo = hoursAgo / 24;
        return daysAgo + " day" + (daysAgo > 1 ? "s" : "") + " ago";
    }

    private void handleEditUser(User user) {
        showUserEditDialog(user);
    }

    private void handleViewUser(User user) {
        showUserViewDialog(user);
    }

    private void handleDeleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete user " + user.getFullName() + "?", ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Delete User");
        confirmAlert.setHeaderText(null);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                if (userDAO.delete(user.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    loadUsers();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete user.");
                }
            }
        });
    }

    private void showUserEditDialog(User user) {
        // Implementation for editing a user
    }

    private void showUserViewDialog(User user) {
        // Implementation for viewing user details
    }

    @FXML
    public void handleAddUser(ActionEvent event) {
        showAddUserDialog();
    }

    private void showAddUserDialog() {
        // Implementation for adding a new user
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
        permissionsField.setEditable(false);
        permissionsField.setStyle("-fx-background-color: #f0f0f0;");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Active", "Inactive");
        statusComboBox.setValue("Active");

        // Helper to set permissions text based on role
        java.util.function.Consumer<UserRole> applyPermissionsForRole = role -> {
            if (role == null) {
                permissionsField.clear();
                return;
            }
            switch (role) {
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
        };

        // Initialize and react to role changes
        applyPermissionsForRole.accept(roleComboBox.getValue());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyPermissionsForRole.accept(newVal));

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

    private String generateNextUserId() {
        return "U" + String.format("%03d", userDAO.findAll().size() + 1);
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
        System.out.println("Navigating to Activity Logs view...");
        try {
            loadView(event, "/fxml/admin-activity-view.fxml");
            System.out.println("Activity Logs view loaded successfully");
        } catch (Exception e) {
            System.err.println("Error navigating to Activity Logs: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                    "Could not navigate to Activity Logs view: " + e.getMessage());
        }
    }
    
    /**
     * Handles the "View All" button click in Recent System Activity section
     * Navigates to the Activity Logs view
     */
    @FXML
    private void handleViewAllActivity(ActionEvent event) {
        System.out.println("View All Activity button clicked - navigating to Activity Logs...");
        handleNavActivity(event);
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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateUserInfo() {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Try to update user info if the controller supports it
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).updateUserInfo();
            } else if (controller instanceof AdminActivityController) {
                ((AdminActivityController) controller).updateUserInfo();
            }

            // 1. Set initial opacity to 0 (Invisible)
            root.setOpacity(0);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            if (stage == null || stage.getScene() == null) {
                showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not access window.");
                return;
            }
            
            stage.getScene().setRoot(root);

            // 2. Play Fade Transition (0.0 -> 1.0)
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
