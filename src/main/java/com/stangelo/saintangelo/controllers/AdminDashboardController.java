package com.stangelo.saintangelo.controllers;

import com.stangelo.saintangelo.dao.PatientDAO;
import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.dao.UserDAO;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

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

    private UserDAO userDAO;
    private PatientDAO patientDAO;
    private TicketDAO ticketDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();
        patientDAO = new PatientDAO();
        ticketDAO = new TicketDAO();

        updateUserInfo();

        if (userTableContainer != null) {
            initializeUserManagement();
        }

        if (totalUsersChart != null) {
            initializeDashboardCharts();
        }
    }

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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
