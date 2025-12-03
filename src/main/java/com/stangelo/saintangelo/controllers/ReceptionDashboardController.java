package com.stangelo.saintangelo.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.stangelo.saintangelo.dao.PatientDAO;
import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.models.Discharge;
import com.stangelo.saintangelo.models.DischargeStatus;
import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Prescription;
import com.stangelo.saintangelo.models.PriorityLevel;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.services.QueueManager;
import com.stangelo.saintangelo.services.QueueService;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ReceptionDashboardController implements Initializable {

    // --- FXML INJECTIONS ---
    @FXML private Button btnTabNew;
    @FXML private Button btnTabExisting;
    @FXML private VBox formNewPatient;
    @FXML private VBox formExistingPatient;
    @FXML private Label queueNumberLabel;
    @FXML private Label ticketNumberLabel;
    @FXML private Label assignedDoctorLabel;
    @FXML private VBox queueListContainer;
    @FXML private VBox recentCallsContainer;
    
    // Stats labels
    @FXML private Label totalTodayLabel;
    @FXML private Label waitingCountLabel;
    @FXML private Label avgWaitTimeLabel;
    
    // Stats charts
    @FXML private LineChart<String, Number> totalTodayChart;
    @FXML private LineChart<String, Number> waitingCountChart;
    @FXML private LineChart<String, Number> avgWaitTimeChart;
    
    // Stats footers
    @FXML private Label totalTodayFooter;
    @FXML private Label waitingCountFooter;
    @FXML private Label avgWaitTimeFooter;
    
    // Queue Management Tab Containers
    @FXML private javafx.scene.control.TabPane queueTabPane;
    @FXML private VBox waitingQueueContainer;
    @FXML private VBox inProgressContainer;
    @FXML private VBox completedContainer;

    // New Patient Form Fields
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField ageField;
    @FXML private TextArea chiefComplaintArea;
    @FXML private ComboBox<String> priorityComboBox;

    // Existing Patient Form Fields
    @FXML private TextField searchPhoneField;
    @FXML private VBox existingPatientInfo;
    @FXML private Label existingPatientNameLabel;
    @FXML private Label existingPatientDetailsLabel;
    @FXML private TextArea existingChiefComplaintArea;
    @FXML private ComboBox<String> existingPriorityComboBox;
    @FXML private Button btnRegisterExisting;

    // Discharge Modal
    @FXML private StackPane modalOverlay;
    private Button currentProcessButton; // To track which button opened the modal

    // Discharge View Fields
    @FXML private VBox dischargeTableContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterComboBox;
    
    // Discharge Modal Labels
    @FXML private Label modalPatientName;
    @FXML private Label modalPatientPhone;
    @FXML private Label modalPatientAge;
    @FXML private Label modalDoctorAssigned;
    @FXML private Label modalChiefComplaint;
    @FXML private Label modalPrescription;
    @FXML private Label modalConsultationDate;
    @FXML private Label modalConsultationNotes;

    // DAOs
    private PatientDAO patientDAO;
    private TicketDAO ticketDAO;
    private com.stangelo.saintangelo.dao.DischargeDAO dischargeDAO;
    private com.stangelo.saintangelo.dao.PrescriptionDAO prescriptionDAO;
    
    // Currently selected existing patient
    private Patient selectedExistingPatient;
    
    // Currently selected discharge for modal
    private com.stangelo.saintangelo.models.Discharge currentDischarge;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize DAOs
        patientDAO = new PatientDAO();
        ticketDAO = new TicketDAO();
        dischargeDAO = new com.stangelo.saintangelo.dao.DischargeDAO();
        prescriptionDAO = new com.stangelo.saintangelo.dao.PrescriptionDAO();
        
        // Initialize discharge view if components are available
        if (dischargeTableContainer != null) {
            initializeDischargeView();
        }
        
        // Bind queue number label to the currently serving ticket
        if (queueNumberLabel != null) {
            queueNumberLabel.textProperty().bind(QueueService.currentlyServingNumberBinding());
        }
        // Also bind ticket number label (on ticket view)
        if (ticketNumberLabel != null) {
            ticketNumberLabel.textProperty().bind(QueueService.lastTicketAsStringBinding());
        }
        // Bind assigned doctor label
        if (assignedDoctorLabel != null) {
            assignedDoctorLabel.textProperty().bind(QueueService.assignedDoctorBinding());
        }
        
        // Initialize priority combo boxes
        if (priorityComboBox != null) {
            priorityComboBox.getItems().addAll("Regular", "Senior Citizen", "Emergency");
            priorityComboBox.setValue("Regular");
        }
        if (existingPriorityComboBox != null) {
            existingPriorityComboBox.getItems().addAll("Regular", "Senior Citizen", "Emergency");
            existingPriorityComboBox.setValue("Regular");
        }
        
        // Setup queue list listener
        if (queueListContainer != null) {
            QueueService.getWaitingQueue().addListener((ListChangeListener<Ticket>) change -> {
                updateQueueDisplay();
            });
            // Sync from database and refresh display
            QueueService.syncFromDatabase();
            updateQueueDisplay();
        }
        
        // Load queue management data if containers are available
        if (waitingQueueContainer != null || inProgressContainer != null || completedContainer != null) {
            loadQueueManagementData();
        }
        
        // Load recent calls if container is available
        if (recentCallsContainer != null) {
            loadRecentCalls();
        }
        
        // Sync from database first, then load and update stats (same as MedicalDashboardController)
        QueueService.syncFromDatabase();
        
        // Update stats after a small delay to ensure UI is fully initialized
        javafx.application.Platform.runLater(() -> {
            updateStats();
            updateCharts();
            updateFooters();
        });
    }
    
    /**
     * Refreshes queue data from QueueManager and updates UI
     */
    private void refreshQueueData() {
        QueueService.syncFromDatabase();
        updateQueueDisplay();
        // Also refresh recent calls
        if (recentCallsContainer != null) {
            loadRecentCalls();
        }
        // Update stats
        updateStats();
        updateCharts();
        updateFooters();
    }
    
    /**
     * Updates the stats cards with data from the database
     * Gets values directly from DAO to ensure fresh data
     */
    private void updateStats() {
        if (ticketDAO == null) {
            return;
        }
        
        // Sync queue first to ensure QueueManager is up to date
        QueueService.syncFromDatabase();
        
        // Get values directly from database
        int totalToday = ticketDAO.countTodayTickets();
        int avgWaitTime = ticketDAO.getAverageWaitTimeToday();
        int waitingCount = QueueManager.getInstance().size();
        
        // Update Total Today - ensure label exists and set text directly
        if (totalTodayLabel != null) {
            String totalTodayText = String.valueOf(totalToday);
            totalTodayLabel.setText(totalTodayText);
            // Ensure label has enough width to display text
            totalTodayLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            totalTodayLabel.setMaxWidth(Double.MAX_VALUE);
        }
        
        // Update Average Wait Time
        if (avgWaitTimeLabel != null) {
            String avgWaitTimeText = (avgWaitTime > 0) ? (avgWaitTime + " min") : "0 min";
            avgWaitTimeLabel.setText(avgWaitTimeText);
            // Ensure label has enough width to display text
            avgWaitTimeLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            avgWaitTimeLabel.setMaxWidth(Double.MAX_VALUE);
        }
        
        // Update Waiting count
        if (waitingCountLabel != null) {
            String waitingCountText = String.valueOf(waitingCount);
            waitingCountLabel.setText(waitingCountText);
            // Ensure label has enough width to display text
            waitingCountLabel.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
            waitingCountLabel.setMaxWidth(Double.MAX_VALUE);
        }
    }
    
    /**
     * Updates the line charts with data from the last 7 days
     */
    private void updateCharts() {
        if (ticketDAO == null) {
            return;
        }
        
        // Update Total Today Chart
        if (totalTodayChart != null) {
            updateLineChart(totalTodayChart, ticketDAO.getDailyTicketCountsLast7Days(), "#0b7d56");
        }
        
        // Update Waiting Count Chart
        if (waitingCountChart != null) {
            updateLineChart(waitingCountChart, ticketDAO.getDailyWaitingCountsLast7Days(), "#76ff03");
        }
        
        // Update Average Wait Time Chart
        if (avgWaitTimeChart != null) {
            updateLineChart(avgWaitTimeChart, ticketDAO.getDailyAverageWaitTimesLast7Days(), "#64ffda");
        }
    }
    
    /**
     * Helper method to update a line chart with daily data
     */
    private void updateLineChart(LineChart<String, Number> chart, java.util.Map<java.time.LocalDate, Integer> data, String color) {
        chart.getData().clear();
        
        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        // Format dates and add data points
        java.time.format.DateTimeFormatter dayFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE");
        for (java.util.Map.Entry<java.time.LocalDate, Integer> entry : data.entrySet()) {
            String dayLabel = entry.getKey().format(dayFormatter);
            series.getData().add(new XYChart.Data<>(dayLabel, entry.getValue()));
        }
        
        chart.getData().add(series);
        
        // Style the chart
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAnimated(true); // Enable animation for smooth on-load effect
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        
        // Hide axes
        chart.getXAxis().setVisible(false);
        chart.getYAxis().setVisible(false);
        
        // Style the line using CSS
        String chartId = chart.getId();
        if (chartId == null || chartId.isEmpty()) {
            chartId = "chart-" + System.identityHashCode(chart);
            chart.setId(chartId);
        }
        
        // Apply style to the series line
        javafx.application.Platform.runLater(() -> {
            javafx.scene.Node line = series.getNode().lookup(".chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            }
            // Also style via CSS lookup
            javafx.scene.Node chartNode = chart.lookup(".chart-series-line");
            if (chartNode != null) {
                chartNode.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2px;");
            }
        });
    }
    
    /**
     * Updates footer labels with comparison data based on graph data
     */
    private void updateFooters() {
        if (ticketDAO == null) {
            return;
        }
        
        // Update Total Today Footer
        if (totalTodayFooter != null) {
            double currentAvg = ticketDAO.getDailyTicketCountsLast7Days().values().stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);
            double previousAvg = ticketDAO.getAverageTicketCountPrevious7Days();
            updateFooterLabel(totalTodayFooter, currentAvg, previousAvg);
        }
        
        // Update Waiting Count Footer
        if (waitingCountFooter != null) {
            double currentAvg = ticketDAO.getDailyWaitingCountsLast7Days().values().stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);
            double previousAvg = ticketDAO.getAverageWaitingCountPrevious7Days();
            updateFooterLabel(waitingCountFooter, currentAvg, previousAvg);
        }
        
        // Update Average Wait Time Footer
        if (avgWaitTimeFooter != null) {
            double currentAvg = ticketDAO.getDailyAverageWaitTimesLast7Days().values().stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);
            double previousAvg = ticketDAO.getAverageWaitTimePrevious7Days();
            updateFooterLabel(avgWaitTimeFooter, currentAvg, previousAvg);
        }
    }
    
    /**
     * Helper method to format footer text with comparison
     */
    private void updateFooterLabel(Label footer, double current, double previous) {
        if (previous == 0.0) {
            footer.setText("No previous data available");
            return;
        }
        
        double difference = current - previous;
        double percentChange = (difference / previous) * 100;
        
        String sign = difference >= 0 ? "+" : "";
        String direction = difference >= 0 ? "Increased" : "Decreased";
        int roundedDiff = (int) Math.round(Math.abs(difference));
        int roundedPercent = (int) Math.round(Math.abs(percentChange));
        
        footer.setText(String.format("%s%d %s vs Last Week (%d%%)", 
            sign, roundedDiff, direction, roundedPercent));
    }
    
    /**
     * Updates the queue display with current waiting tickets
     */
    private void updateQueueDisplay() {
        if (queueListContainer == null) return;
        
        queueListContainer.getChildren().clear();
        
        var waitingTickets = QueueService.getWaitingQueue();
        
        if (waitingTickets.isEmpty()) {
            Label emptyLabel = new Label("No patients in queue");
            emptyLabel.getStyleClass().add("queue-empty-label");
            queueListContainer.getChildren().add(emptyLabel);
        } else {
            for (Ticket ticket : waitingTickets) {
                HBox queueItem = createQueueItemCard(ticket);
                queueListContainer.getChildren().add(queueItem);
            }
        }
    }
    
    /**
     * Creates a queue item card for display
     */
    private HBox createQueueItemCard(Ticket ticket) {
        HBox card = new HBox(15);
        card.getStyleClass().add("queue-item-card");
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Ticket number
        Label ticketNum = new Label(ticket.getTicketNumber());
        ticketNum.getStyleClass().add("queue-ticket-number");
        
        // Patient name
        String patientName = ticket.getPatient() != null ? ticket.getPatient().getName() : "Unknown";
        Label nameLabel = new Label(patientName);
        nameLabel.getStyleClass().add("queue-patient-name");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Priority badge
        Label priorityBadge = new Label(getPriorityDisplayText(ticket.getPriority()));
        priorityBadge.getStyleClass().add(getPriorityStyleClass(ticket.getPriority()));
        
        card.getChildren().addAll(ticketNum, nameLabel, spacer, priorityBadge);
        
        return card;
    }
    
    /**
     * Gets display text for priority level
     */
    private String getPriorityDisplayText(PriorityLevel priority) {
        if (priority == null) return "Regular";
        switch (priority) {
            case EMERGENCY: return "Emergency";
            case SENIOR_CITIZEN: return "Senior";
            default: return "Regular";
        }
    }
    
    /**
     * Gets CSS style class for priority badge
     */
    private String getPriorityStyleClass(PriorityLevel priority) {
        if (priority == null) return "badge-priority-regular";
        switch (priority) {
            case EMERGENCY: return "badge-priority-emergency";
            case SENIOR_CITIZEN: return "badge-priority-senior";
            default: return "badge-priority-regular";
        }
    }
    
    // --- QUEUE HANDLERS ---
    @FXML
    private void handleRefreshQueue(ActionEvent event) {
        refreshQueueData();
    }

    // --- DISCHARGE MODAL HANDLERS ---

    @FXML
    private void handleProcess(ActionEvent event) {
        // Store reference to the button that was clicked
        currentProcessButton = (Button) event.getSource();
        
        // Get ticket from button's user data
        Ticket ticket = (Ticket) currentProcessButton.getUserData();
        
        if (ticket == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "No ticket found.");
            return;
        }
        
        // Check if discharge record already exists
        Discharge existingDischarge = dischargeDAO.findByPatientId(ticket.getPatient().getId());
        
        if (existingDischarge == null) {
            // Create new discharge record
            String dischargeId = "D" + System.currentTimeMillis() % 100000;
            String department = ticketDAO.getDoctorDepartment(ticket.getAssignedDoctorId());
            if (department == null || department.isEmpty()) {
                department = "General";
            }
            
            // Get prescription for this specific visit (patient + assigned doctor)
            Prescription latestPrescription = null;
            if (ticket.getAssignedDoctorId() != null && !ticket.getAssignedDoctorId().isEmpty()) {
                List<Prescription> prescriptions = prescriptionDAO.findByPatientAndDoctor(
                    ticket.getPatient().getId(), 
                    ticket.getAssignedDoctorId()
                );
                latestPrescription = prescriptions.isEmpty() ? null : prescriptions.get(0);
            }
            
            // Fallback: if no prescription found for this doctor, try to get latest for patient
            if (latestPrescription == null) {
                List<Prescription> allPrescriptions = prescriptionDAO.findByPatient(ticket.getPatient().getId());
                latestPrescription = allPrescriptions.isEmpty() ? null : allPrescriptions.get(0);
            }
            
            Discharge newDischarge = new Discharge(
                dischargeId,
                ticket.getPatient(),
                department,
                DischargeStatus.PENDING_REVIEW,
                null, // discharge date
                latestPrescription,
                null, // billing amount
                null  // notes
            );
            
            boolean created = dischargeDAO.create(newDischarge);
            if (!created) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create discharge record.");
                return;
            }
            
            currentDischarge = newDischarge;
        } else {
            currentDischarge = existingDischarge;
        }
        
        // Populate modal with patient information from ticket
        // Always get fresh prescription from database to ensure all fields are loaded
        populateModal(ticket);

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
    private void handleSearch(ActionEvent event) {
        loadDischargeData();
    }
    
    @FXML
    private void handleStatusFilter(ActionEvent event) {
        loadDischargeData();
    }

    @FXML
    private void handleCloseModal(ActionEvent event) {
        closeModal();
    }

    @FXML
    private void handleReviewed(ActionEvent event) {
        if (currentDischarge == null) {
            closeModal();
            return;
        }
        
        // Update discharge status to READY
        boolean updated = dischargeDAO.updateStatus(currentDischarge.getDischargeId(), DischargeStatus.READY);
        
        if (updated) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Patient information reviewed. Status updated to Ready.");
            closeModal();
            currentDischarge = null; // Clear current discharge
            loadDischargeData(); // Refresh the table
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update discharge status.");
        }
    }
    
    @FXML
    private void handleDischarge(ActionEvent event) {
        Button btn = (Button) event.getSource();
        Discharge discharge = (Discharge) btn.getUserData();
        
        if (discharge == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "No discharge record found.");
            return;
        }
        
        // Confirm discharge
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Discharge");
        confirmAlert.setHeaderText("Discharge Patient?");
        confirmAlert.setContentText("Are you sure you want to discharge " + 
            (discharge.getPatient() != null ? discharge.getPatient().getName() : "this patient") + "?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Update status to DISCHARGED and set discharge date
                boolean updated = dischargeDAO.updateStatusAndDate(
                    discharge.getDischargeId(), 
                    DischargeStatus.DISCHARGED,
                    java.time.LocalDateTime.now()
                );
                
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Patient discharged successfully.");
                    loadDischargeData(); // Refresh the table
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to discharge patient.");
                }
            }
        });
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

    // --- PATIENT REGISTRATION HANDLERS ---

    @FXML
    private void handleRegisterPatient(ActionEvent event) {
        // Validate required fields
        if (firstNameField == null || lastNameField == null || phoneNumberField == null || ageField == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Form fields not properly initialized.");
            return;
        }
        
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phoneNumber = phoneNumberField.getText().trim();
        String ageText = ageField.getText().trim();
        String chiefComplaint = chiefComplaintArea != null ? chiefComplaintArea.getText().trim() : "";
        String priorityStr = priorityComboBox != null && priorityComboBox.getValue() != null ? 
                         priorityComboBox.getValue() : "Regular";

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter the patient's first and last name.");
            return;
        }
        if (phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter the patient's phone number.");
            return;
        }
        if (ageText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter the patient's age.");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age < 0 || age > 150) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Please enter a valid age (0-150).");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Age must be a number.");
            return;
        }

        // Generate patient ID
        String patientId = generatePatientId();
        
        // Determine priority level
        PriorityLevel priority = mapStringToPriority(priorityStr, age);
        boolean isSeniorCitizen = age >= 60 || priority == PriorityLevel.SENIOR_CITIZEN;
        
        // Create Patient object
        String fullName = firstName + " " + lastName;
        Patient patient = new Patient(
            patientId, fullName, age, phoneNumber, null, null,
            null, null, isSeniorCitizen,
            null, null, null, null, chiefComplaint,
            null, null, null, null, null, null,
            LocalDate.now().toString(), null
        );

        // Save patient to database
        boolean patientSaved = patientDAO.create(patient);
        
        if (patientSaved) {
            // Generate ticket number
            String ticketNumber = QueueService.generateNextTicket(fullName);
            
            // Create ticket object
            Ticket ticket = new Ticket(
                generateVisitId(),
                ticketNumber,
                patient,
                com.stangelo.saintangelo.models.TicketStatus.WAITING,
                priority,
                java.time.LocalDateTime.now(),
                null,
                chiefComplaint,
                null,
                null
            );
            
            // Enqueue the ticket (uses PriorityQueue and persists to database)
            boolean enqueued = QueueService.enqueue(ticket);
            
            if (enqueued) {
                // Refresh stats after creating a new ticket
                updateStats();
                
                // Clear form
                clearNewPatientForm();
                
                // Navigate to ticket view to show the generated ticket
                navigateToTicketView(event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add patient to queue. Please try again.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to register patient. Please try again.");
        }
    }
    
    /**
     * Maps priority string from combo box to PriorityLevel enum
     */
    private PriorityLevel mapStringToPriority(String priorityStr, int age) {
        if ("Emergency".equals(priorityStr)) {
            return PriorityLevel.EMERGENCY;
        } else if ("Senior Citizen".equals(priorityStr) || age >= 60) {
            return PriorityLevel.SENIOR_CITIZEN;
        }
        return PriorityLevel.REGULAR;
    }
    
    /**
     * Generates a unique visit ID for tickets
     */
    private String generateVisitId() {
        return "V" + System.currentTimeMillis() % 100000;
    }

    @FXML
    private void handleSearchPatient(ActionEvent event) {
        if (searchPhoneField == null) return;
        
        String phoneNumber = searchPhoneField.getText().trim();
        if (phoneNumber.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Search Error", "Please enter a phone number to search.");
            return;
        }
        
        // Search for patient
        selectedExistingPatient = patientDAO.findByPhoneNumber(phoneNumber);
        
        if (selectedExistingPatient != null) {
            // Show patient info
            if (existingPatientInfo != null) {
                existingPatientInfo.setVisible(true);
                existingPatientInfo.setManaged(true);
            }
            if (existingPatientNameLabel != null) {
                existingPatientNameLabel.setText(selectedExistingPatient.getName());
            }
            if (existingPatientDetailsLabel != null) {
                existingPatientDetailsLabel.setText("Age: " + selectedExistingPatient.getAge() + 
                    " | Phone: " + selectedExistingPatient.getContactNumber());
            }
            if (btnRegisterExisting != null) {
                btnRegisterExisting.setDisable(false);
            }
        } else {
            // Hide patient info
            if (existingPatientInfo != null) {
                existingPatientInfo.setVisible(false);
                existingPatientInfo.setManaged(false);
            }
            if (btnRegisterExisting != null) {
                btnRegisterExisting.setDisable(true);
            }
            showAlert(Alert.AlertType.INFORMATION, "Not Found", 
                "No patient found with phone number: " + phoneNumber + "\n\n" +
                "Please use the 'Patient Registration Form' tab to register a new patient.");
        }
    }

    @FXML
    private void handleRegisterExistingPatient(ActionEvent event) {
        if (selectedExistingPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Please search for a patient first.");
            return;
        }
        
        // Get chief complaint and priority
        String chiefComplaint = existingChiefComplaintArea != null ? existingChiefComplaintArea.getText().trim() : "";
        String priorityStr = existingPriorityComboBox != null && existingPriorityComboBox.getValue() != null ?
                            existingPriorityComboBox.getValue() : "Regular";
        
        // Update patient's last visit date
        patientDAO.updateLastVisitDate(selectedExistingPatient.getId(), LocalDate.now());
        
        // Determine priority level
        PriorityLevel priority = mapStringToPriority(priorityStr, selectedExistingPatient.getAge());
        
        // Generate ticket number
        String ticketNumber = QueueService.generateNextTicket(selectedExistingPatient.getName());
        
        // Create ticket object
        Ticket ticket = new Ticket(
            generateVisitId(),
            ticketNumber,
            selectedExistingPatient,
            com.stangelo.saintangelo.models.TicketStatus.WAITING,
            priority,
            java.time.LocalDateTime.now(),
            null,
            chiefComplaint,
            null,
            null
        );
        
        // Enqueue the ticket (uses PriorityQueue and persists to database)
        boolean enqueued = QueueService.enqueue(ticket);
        
        if (enqueued) {
            // Refresh stats after creating a new ticket
            updateStats();
            
            // Clear form
            clearExistingPatientForm();
            
            // Navigate to ticket view to show the generated ticket
            navigateToTicketView(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add patient to queue. Please try again.");
        }
    }

    // --- HELPER METHODS FOR REGISTRATION ---
    
    private String generatePatientId() {
        // Generate unique patient ID: P + timestamp + random
        return "P" + System.currentTimeMillis() % 100000;
    }
    
    /**
     * Navigates to the ticket view to display the generated ticket
     */
    private void navigateToTicketView(ActionEvent event) {
        loadView(event, "/fxml/reception-ticket-view.fxml");
    }
    
    private void clearNewPatientForm() {
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (phoneNumberField != null) phoneNumberField.clear();
        if (ageField != null) ageField.clear();
        if (chiefComplaintArea != null) chiefComplaintArea.clear();
        if (priorityComboBox != null) priorityComboBox.setValue("Regular");
    }
    
    private void clearExistingPatientForm() {
        if (searchPhoneField != null) searchPhoneField.clear();
        if (existingChiefComplaintArea != null) existingChiefComplaintArea.clear();
        if (existingPriorityComboBox != null) existingPriorityComboBox.setValue("Regular");
        if (existingPatientInfo != null) {
            existingPatientInfo.setVisible(false);
            existingPatientInfo.setManaged(false);
        }
        if (btnRegisterExisting != null) btnRegisterExisting.setDisable(true);
        selectedExistingPatient = null;
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
        // Refresh data after view loads
        javafx.application.Platform.runLater(() -> {
            QueueService.syncFromDatabase();
            updateStats();
            updateCharts();
            updateFooters();
            if (recentCallsContainer != null) {
                loadRecentCalls();
            }
            if (queueListContainer != null) {
                updateQueueDisplay();
            }
        });
    }

    @FXML
    private void handleNavRegistration(ActionEvent event) {
        loadView(event, "/fxml/receptionist-registration-view.fxml");
    }

    @FXML
    private void handleNavQueue(ActionEvent event) {
        loadView(event, "/fxml/receptionist-queueManagement-view.fxml");
        // Refresh queue data after view loads
        javafx.application.Platform.runLater(() -> {
            loadQueueManagementData();
        });
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
            titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 0;");

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

    // --- QUEUE MANAGEMENT METHODS ---
    
    /**
     * Loads and displays queue management data for all three tabs
     */
    private void loadQueueManagementData() {
        if (waitingQueueContainer != null) {
            loadWaitingQueue();
        }
        if (inProgressContainer != null) {
            loadInProgressQueue();
        }
        if (completedContainer != null) {
            loadCompletedQueue();
        }
    }
    
    /**
     * Loads and displays waiting queue tickets
     * Uses QueueManager to get tickets in the same order as Dashboard
     */
    private void loadWaitingQueue() {
        if (waitingQueueContainer == null) return;
        
        waitingQueueContainer.getChildren().clear();
        
        // Sync from database first to ensure QueueManager has latest data
        QueueService.syncFromDatabase();
        
        // Get all waiting tickets from QueueManager (uses PriorityQueue ordering)
        List<Ticket> waitingTickets = QueueManager.getInstance().getAllWaiting();
        
        if (waitingTickets.isEmpty()) {
            Label emptyLabel = new Label("No patients waiting");
            emptyLabel.getStyleClass().add("queue-empty-label");
            waitingQueueContainer.getChildren().add(emptyLabel);
        } else {
            int position = 1;
            for (Ticket ticket : waitingTickets) {
                HBox card = createWaitingQueueCard(ticket, position++);
                waitingQueueContainer.getChildren().add(card);
            }
        }
    }
    
    /**
     * Creates a card for a waiting queue ticket
     */
    private HBox createWaitingQueueCard(Ticket ticket, int position) {
        HBox card = new HBox(20);
        card.getStyleClass().addAll("q-card", "q-card-waiting");
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Queue position number
        StackPane positionCircle = new StackPane();
        positionCircle.getStyleClass().add("queue-number-circle");
        Label positionLabel = new Label(String.valueOf(position));
        positionLabel.getStyleClass().add("queue-number-text");
        positionCircle.getChildren().add(positionLabel);
        
        // Patient info
        VBox patientInfo = new VBox(5);
        patientInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(patientInfo, Priority.ALWAYS);
        
        Patient patient = ticket.getPatient();
        String patientName = patient != null ? patient.getName() : "Unknown";
        int age = patient != null ? patient.getAge() : 0;
        String ticketNumber = ticket.getTicketNumber();
        String chiefComplaint = ticket.getServiceType() != null ? ticket.getServiceType() : "N/A";
        String phoneNumber = patient != null && patient.getContactNumber() != null ? 
                            patient.getContactNumber() : "N/A";
        
        // Name, age, priority badge
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(patientName);
        nameLabel.getStyleClass().add("text-name");
        Label ageLabel = new Label("• Age " + age);
        ageLabel.getStyleClass().add("text-meta");
        Label priorityBadge = new Label(getPriorityDisplayText(ticket.getPriority()));
        priorityBadge.getStyleClass().addAll("badge-pill", getPriorityBadgeStyleClass(ticket.getPriority()));
        nameRow.getChildren().addAll(nameLabel, ageLabel, priorityBadge);
        
        // Ticket ID, complaint, phone
        HBox detailsRow = new HBox(15);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        Label ticketIdLabel = new Label("ID: " + ticketNumber);
        ticketIdLabel.getStyleClass().add("text-sub");
        Label complaintLabel = new Label("• " + chiefComplaint);
        complaintLabel.getStyleClass().add("text-sub");
        Label phoneLabel = new Label("📞 " + phoneNumber);
        phoneLabel.getStyleClass().add("text-sub");
        detailsRow.getChildren().addAll(ticketIdLabel, complaintLabel, phoneLabel);
        
        patientInfo.getChildren().addAll(nameRow, detailsRow);
        
        // Wait time
        VBox waitTimeBox = new VBox(5);
        waitTimeBox.setAlignment(Pos.CENTER_RIGHT);
        Label waitTimeLabel = new Label("Wait Time");
        waitTimeLabel.getStyleClass().add("text-meta");
        long waitMinutes = java.time.Duration.between(ticket.getCreatedTime(), java.time.LocalDateTime.now()).toMinutes();
        Label waitTimeValue = new Label(waitMinutes + " min");
        waitTimeValue.getStyleClass().add("wait-time-text");
        if (ticket.getPriority() == PriorityLevel.EMERGENCY) {
            waitTimeValue.setStyle("-fx-text-fill: #e74c3c;");
        }
        waitTimeBox.getChildren().addAll(waitTimeLabel, waitTimeValue);
        
        // Remove button
        Button removeBtn = new Button();
        removeBtn.getStyleClass().add("icon-btn-trash");
        removeBtn.setOnAction(e -> handleRemoveTicket(ticket));
        javafx.scene.shape.SVGPath trashIcon = new javafx.scene.shape.SVGPath();
        trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashIcon.setFill(javafx.scene.paint.Color.WHITE);
        trashIcon.setScaleX(0.7);
        trashIcon.setScaleY(0.7);
        removeBtn.setGraphic(trashIcon);
        
        // Escalate button
        Button escalateBtn = new Button();
        escalateBtn.getStyleClass().add("icon-btn-escalate");
        escalateBtn.setOnAction(e -> handleEscalatePriority(ticket));
        try {
            javafx.scene.image.ImageView escalateIcon = new javafx.scene.image.ImageView(
                new javafx.scene.image.Image(getClass().getResourceAsStream("/images/escalate.png"))
            );
            escalateIcon.setFitHeight(20);
            escalateIcon.setFitWidth(20);
            escalateIcon.setOpacity(0.8);
            escalateBtn.setGraphic(escalateIcon);
        } catch (Exception e) {
            // If image not found, use text label
            escalateBtn.setText("↑");
            escalateBtn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.getChildren().addAll(removeBtn, escalateBtn);
        
        card.getChildren().addAll(positionCircle, patientInfo, waitTimeBox, actionButtons);
        
        return card;
    }
    
    /**
     * Loads and displays in-progress tickets
     */
    private void loadInProgressQueue() {
        if (inProgressContainer == null) return;
        
        inProgressContainer.getChildren().clear();
        
        List<Ticket> inProgressTickets = ticketDAO.findInServiceTickets();
        
        if (inProgressTickets.isEmpty()) {
            Label emptyLabel = new Label("No patients currently in service");
            emptyLabel.getStyleClass().add("queue-empty-label");
            inProgressContainer.getChildren().add(emptyLabel);
        } else {
            for (Ticket ticket : inProgressTickets) {
                HBox card = createInProgressCard(ticket);
                inProgressContainer.getChildren().add(card);
            }
        }
    }
    
    /**
     * Creates a card for an in-progress ticket
     */
    private HBox createInProgressCard(Ticket ticket) {
        HBox card = new HBox(20);
        card.getStyleClass().addAll("q-card", "q-card-progress");
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Avatar circle
        StackPane avatarPane = new StackPane();
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(25, javafx.scene.paint.Color.valueOf("#e3f2fd"));
        javafx.scene.shape.SVGPath avatarIcon = new javafx.scene.shape.SVGPath();
        avatarIcon.setContent("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z");
        avatarIcon.setScaleX(1.5);
        avatarIcon.setScaleY(1.5);
        avatarIcon.getStyleClass().add("avatar-circle-blue");
        avatarPane.getChildren().addAll(circle, avatarIcon);
        
        // Patient info
        VBox patientInfo = new VBox(5);
        patientInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(patientInfo, Priority.ALWAYS);
        
        Patient patient = ticket.getPatient();
        String patientName = patient != null ? patient.getName() : "Unknown";
        int age = patient != null ? patient.getAge() : 0;
        String ticketNumber = ticket.getTicketNumber();
        String chiefComplaint = ticket.getServiceType() != null ? ticket.getServiceType() : "N/A";
        String doctorName = ticket.getAssignedDoctorName() != null ? ticket.getAssignedDoctorName() : "Unassigned";
        
        // Name, age, status badge
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(patientName);
        nameLabel.getStyleClass().add("text-name");
        Label ageLabel = new Label("• Age " + age);
        ageLabel.getStyleClass().add("text-meta");
        Label statusBadge = new Label("Consulting");
        statusBadge.getStyleClass().addAll("badge-pill", "badge-consulting");
        nameRow.getChildren().addAll(nameLabel, ageLabel, statusBadge);
        
        // Ticket ID, complaint, doctor
        HBox detailsRow = new HBox(15);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        Label ticketIdLabel = new Label("ID: " + ticketNumber);
        ticketIdLabel.getStyleClass().add("text-sub");
        Label complaintLabel = new Label("• " + chiefComplaint);
        complaintLabel.getStyleClass().add("text-sub");
        Label doctorLabel = new Label("• Dr. " + doctorName);
        doctorLabel.getStyleClass().add("text-sub");
        detailsRow.getChildren().addAll(ticketIdLabel, complaintLabel, doctorLabel);
        
        patientInfo.getChildren().addAll(nameRow, detailsRow);
        
        // Delete button
        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("icon-btn-trash");
        deleteBtn.setOnAction(e -> handleDeleteInProgressTicket(ticket));
        javafx.scene.shape.SVGPath trashIcon = new javafx.scene.shape.SVGPath();
        trashIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        trashIcon.setFill(javafx.scene.paint.Color.WHITE);
        trashIcon.setScaleX(0.7);
        trashIcon.setScaleY(0.7);
        deleteBtn.setGraphic(trashIcon);
        
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.getChildren().add(deleteBtn);
        
        card.getChildren().addAll(avatarPane, patientInfo, actionButtons);
        
        return card;
    }
    
    /**
     * Loads and displays completed tickets for today
     */
    private void loadCompletedQueue() {
        if (completedContainer == null) return;
        
        completedContainer.getChildren().clear();
        
        List<Ticket> completedTickets = ticketDAO.findCompletedToday();
        
        if (completedTickets.isEmpty()) {
            Label emptyLabel = new Label("No completed visits today");
            emptyLabel.getStyleClass().add("queue-empty-label");
            completedContainer.getChildren().add(emptyLabel);
        } else {
            for (Ticket ticket : completedTickets) {
                HBox card = createCompletedCard(ticket);
                completedContainer.getChildren().add(card);
            }
        }
    }
    
    /**
     * Creates a card for a completed ticket
     */
    private HBox createCompletedCard(Ticket ticket) {
        HBox card = new HBox(20);
        card.getStyleClass().addAll("q-card", "q-card-completed");
        card.setAlignment(Pos.CENTER_LEFT);
        
        // Checkmark circle
        StackPane checkPane = new StackPane();
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(20, javafx.scene.paint.Color.valueOf("#2e7d32"));
        javafx.scene.shape.SVGPath checkIcon = new javafx.scene.shape.SVGPath();
        checkIcon.setContent("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z");
        checkIcon.setFill(javafx.scene.paint.Color.WHITE);
        checkIcon.setScaleX(0.8);
        checkIcon.setScaleY(0.8);
        checkPane.getChildren().addAll(circle, checkIcon);
        
        // Patient info
        VBox patientInfo = new VBox(5);
        patientInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(patientInfo, Priority.ALWAYS);
        
        Patient patient = ticket.getPatient();
        String patientName = patient != null ? patient.getName() : "Unknown";
        int age = patient != null ? patient.getAge() : 0;
        String ticketNumber = ticket.getTicketNumber();
        String chiefComplaint = ticket.getServiceType() != null ? ticket.getServiceType() : "N/A";
        
        // Format completion time
        String completedTime = "N/A";
        if (ticket.getCalledTime() != null) {
            completedTime = ticket.getCalledTime().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
        }
        
        // Name, age, priority badge
        HBox nameRow = new HBox(10);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(patientName);
        nameLabel.getStyleClass().add("text-name");
        Label ageLabel = new Label("• Age " + age);
        ageLabel.getStyleClass().add("text-meta");
        Label priorityBadge = new Label(getPriorityDisplayText(ticket.getPriority()));
        priorityBadge.getStyleClass().addAll("badge-pill", getPriorityBadgeStyleClass(ticket.getPriority()));
        nameRow.getChildren().addAll(nameLabel, ageLabel, priorityBadge);
        
        // Ticket ID, complaint, completion time
        HBox detailsRow = new HBox(15);
        detailsRow.setAlignment(Pos.CENTER_LEFT);
        Label ticketIdLabel = new Label("ID: " + ticketNumber);
        ticketIdLabel.getStyleClass().add("text-sub");
        Label complaintLabel = new Label("• " + chiefComplaint);
        complaintLabel.getStyleClass().add("text-sub");
        Label timeLabel = new Label("• Completed at " + completedTime);
        timeLabel.getStyleClass().add("text-sub");
        detailsRow.getChildren().addAll(ticketIdLabel, complaintLabel, timeLabel);
        
        patientInfo.getChildren().addAll(nameRow, detailsRow);
        
        // View Details button
        Button viewDetailsBtn = new Button("View Details");
        viewDetailsBtn.getStyleClass().add("btn-view-details");
        viewDetailsBtn.setOnAction(e -> handleViewPatientDetails(ticket));
        
        card.getChildren().addAll(checkPane, patientInfo, viewDetailsBtn);
        
        return card;
    }
    
    /**
     * Gets CSS style class for priority badge
     */
    private String getPriorityBadgeStyleClass(PriorityLevel priority) {
        if (priority == null) return "badge-blue";
        switch (priority) {
            case EMERGENCY: return "badge-red";
            case SENIOR_CITIZEN: return "badge-yellow";
            default: return "badge-blue";
        }
    }
    
    /**
     * Handles removing/skipping a ticket from the waiting queue
     */
    private void handleRemoveTicket(Ticket ticket) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Remove Patient");
        confirmAlert.setHeaderText("Remove Patient from Queue?");
        confirmAlert.setContentText("Are you sure you want to remove " + 
            (ticket.getPatient() != null ? ticket.getPatient().getName() : "this patient") + 
            " from the queue?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean removed = ticketDAO.removeTicket(ticket.getVisitId());
                if (removed) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Patient removed from queue.");
                    // Refresh queue data
                    QueueService.syncFromDatabase();
                    loadQueueManagementData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove patient from queue.");
                }
            }
        });
    }
    
    /**
     * Handles deleting a ticket from the in-progress queue
     */
    private void handleDeleteInProgressTicket(Ticket ticket) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Ticket");
        confirmAlert.setHeaderText("Delete Ticket from In Progress?");
        confirmAlert.setContentText("Are you sure you want to permanently delete the ticket for " + 
            (ticket.getPatient() != null ? ticket.getPatient().getName() : "this patient") + 
            "? This action cannot be undone.");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean deleted = ticketDAO.deleteTicket(ticket.getVisitId());
                if (deleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Ticket deleted successfully.");
                    // Sync with QueueManager and refresh queue data
                    QueueService.syncFromDatabase();
                    loadQueueManagementData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete ticket. It may be referenced by other records.");
                }
            }
        });
    }
    
    /**
     * Handles escalating a ticket's priority to Emergency
     */
    private void handleEscalatePriority(Ticket ticket) {
        if (ticket.getPriority() == PriorityLevel.EMERGENCY) {
            showAlert(Alert.AlertType.INFORMATION, "Already Emergency", 
                "This patient already has Emergency priority.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Escalate Priority");
        confirmAlert.setHeaderText("Escalate to Emergency Priority?");
        confirmAlert.setContentText("Are you sure you want to escalate " + 
            (ticket.getPatient() != null ? ticket.getPatient().getName() : "this patient") + 
            " to Emergency priority?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean updated = ticketDAO.updatePriority(ticket.getVisitId(), PriorityLevel.EMERGENCY);
                if (updated) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Priority escalated to Emergency.");
                    // Refresh queue data
                    QueueService.syncFromDatabase();
                    loadQueueManagementData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to escalate priority.");
                }
            }
        });
    }
    
    /**
     * Handles viewing patient details
     */
    private void handleViewPatientDetails(Ticket ticket) {
        Patient patient = ticket.getPatient();
        if (patient == null) {
            showAlert(Alert.AlertType.WARNING, "No Patient Data", "Patient information not available.");
            return;
        }
        
        // Load full patient data from database
        Patient fullPatient = patientDAO.findById(patient.getId());
        if (fullPatient == null) {
            fullPatient = patient; // Use ticket patient if not found
        }
        
        // Create and show patient details dialog
        showPatientDetailsDialog(fullPatient, ticket);
    }
    
    /**
     * Shows a dialog with patient details
     */
    private void showPatientDetailsDialog(Patient patient, Ticket ticket) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Patient Details");
        dialog.setHeaderText("Patient Information");
        
        javafx.scene.control.DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(createPatientDetailsContent(patient, ticket));
        dialogPane.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Creates the content for patient details dialog
     */
    private javafx.scene.Node createPatientDetailsContent(Patient patient, Ticket ticket) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Patient Information Section
        Label patientHeader = new Label("Patient Information");
        patientHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox patientInfo = new VBox(8);
        patientInfo.getChildren().addAll(
            createDetailRow("Name:", patient.getName()),
            createDetailRow("Age:", String.valueOf(patient.getAge())),
            createDetailRow("Phone:", patient.getContactNumber() != null ? patient.getContactNumber() : "N/A"),
            createDetailRow("Gender:", patient.getGender() != null ? patient.getGender() : "N/A"),
            createDetailRow("Address:", patient.getHomeAddress() != null ? patient.getHomeAddress() : "N/A"),
            createDetailRow("Blood Type:", patient.getBloodType() != null ? patient.getBloodType() : "N/A"),
            createDetailRow("Senior Citizen:", patient.isSeniorCitizen() ? "Yes" : "No")
        );
        
        // Emergency Contact Section
        Label emergencyHeader = new Label("Emergency Contact");
        emergencyHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox emergencyInfo = new VBox(8);
        emergencyInfo.getChildren().addAll(
            createDetailRow("Contact Person:", 
                patient.getEmergencycontactPerson() != null ? patient.getEmergencycontactPerson() : "N/A"),
            createDetailRow("Contact Number:", 
                patient.getEmergencycontactNumber() != null ? patient.getEmergencycontactNumber() : "N/A")
        );
        
        // Medical Information Section
        Label medicalHeader = new Label("Medical Information");
        medicalHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox medicalInfo = new VBox(8);
        medicalInfo.getChildren().addAll(
            createDetailRow("Allergies:", patient.getAllergies() != null ? patient.getAllergies() : "None"),
            createDetailRow("Current Medications:", 
                patient.getCurrentMedications() != null ? patient.getCurrentMedications() : "None"),
            createDetailRow("Diagnosis:", patient.getDiagnosis() != null ? patient.getDiagnosis() : "N/A"),
            createDetailRow("Treatment Plan:", 
                patient.getTreatmentPlan() != null ? patient.getTreatmentPlan() : "N/A"),
            createDetailRow("Notes:", patient.getNotes() != null ? patient.getNotes() : "None")
        );
        
        // Visit Information Section
        Label visitHeader = new Label("Visit Information");
        visitHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox visitInfo = new VBox(8);
        visitInfo.getChildren().addAll(
            createDetailRow("Ticket Number:", ticket.getTicketNumber()),
            createDetailRow("Visit ID:", ticket.getVisitId()),
            createDetailRow("Chief Complaint:", 
                ticket.getServiceType() != null ? ticket.getServiceType() : "N/A"),
            createDetailRow("Priority:", getPriorityDisplayText(ticket.getPriority())),
            createDetailRow("Status:", ticket.getStatus().name()),
            createDetailRow("Created:", 
                ticket.getCreatedTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
        
        content.getChildren().addAll(
            patientHeader, patientInfo,
            emergencyHeader, emergencyInfo,
            medicalHeader, medicalInfo,
            visitHeader, visitInfo
        );
        
        return content;
    }
    
    /**
     * Creates a detail row for patient information
     */
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 150px;");
        Label valueLabel = new Label(value != null ? value : "N/A");
        valueLabel.setWrapText(true);
        row.getChildren().addAll(labelLabel, valueLabel);
        return row;
    }
    
    // --- RECENT CALLS METHODS ---
    
    /**
     * Loads and displays recent calls
     */
    private void loadRecentCalls() {
        if (recentCallsContainer == null) return;
        
        recentCallsContainer.getChildren().clear();
        
        // Get recent calls from database (last 10)
        List<Ticket> recentCalls = ticketDAO.findRecentCalls(10);
        
        if (recentCalls.isEmpty()) {
            Label emptyLabel = new Label("No recent calls");
            emptyLabel.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 20;");
            recentCallsContainer.getChildren().add(emptyLabel);
        } else {
            for (Ticket ticket : recentCalls) {
                VBox card = createRecentCallCard(ticket);
                recentCallsContainer.getChildren().add(card);
            }
        }
    }
    
    /**
     * Creates a card for a recent call
     */
    private VBox createRecentCallCard(Ticket ticket) {
        VBox card = new VBox(5);
        card.getStyleClass().add("recent-call-card");
        
        // Top row: Ticket ID and time
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label ticketIdLabel = new Label(ticket.getTicketNumber());
        ticketIdLabel.getStyleClass().add("ticket-id");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Format called time
        String callTime = "N/A";
        if (ticket.getCalledTime() != null) {
            callTime = ticket.getCalledTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }
        Label timeLabel = new Label(callTime);
        timeLabel.getStyleClass().add("call-time-small");
        
        topRow.getChildren().addAll(ticketIdLabel, spacer, timeLabel);
        
        // Bottom row: Service type and priority badge
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        
        String serviceType = ticket.getServiceType() != null ? ticket.getServiceType() : "Consultation";
        Label categoryLabel = new Label(serviceType);
        categoryLabel.getStyleClass().add("call-category");
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        // Priority badge
        Label priorityBadge = new Label();
        PriorityLevel priority = ticket.getPriority();
        if (priority == PriorityLevel.EMERGENCY) {
            priorityBadge.setText("Emergency");
            priorityBadge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-padding: 2 8; -fx-background-radius: 4; -fx-font-size: 11px; -fx-font-weight: bold;");
        } else {
            priorityBadge.setText(getPriorityDisplayText(priority));
            priorityBadge.getStyleClass().add("badge-regular-call");
        }
        
        bottomRow.getChildren().addAll(categoryLabel, spacer2, priorityBadge);
        
        card.getChildren().addAll(topRow, bottomRow);
        
        return card;
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
    
    // --- DISCHARGE VIEW METHODS ---
    
    /**
     * Initializes the discharge view components
     */
    private void initializeDischargeView() {
        // Initialize status filter combo box
        if (statusFilterComboBox != null) {
            statusFilterComboBox.getItems().addAll("All Status", "Ready", "Pending Review", "Cleared", "Discharged");
            statusFilterComboBox.setValue("All Status");
        }
        
        // Add listener to search field for real-time search
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                loadDischargeData();
            });
        }
        
        // Load initial data
        loadDischargeData();
    }
    
    /**
     * Loads and displays discharge data based on current filters
     * Shows completed tickets that don't have discharge records yet
     */
    private void loadDischargeData() {
        if (dischargeTableContainer == null) return;
        
        dischargeTableContainer.getChildren().clear();
        
        // Get all completed tickets
        List<Ticket> completedTickets = ticketDAO.findAllCompleted();
        
        // Get search term
        String searchTerm = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        String statusFilter = statusFilterComboBox != null && statusFilterComboBox.getValue() != null 
            ? statusFilterComboBox.getValue() : "All Status";
        
        // Filter tickets: exclude those that already have discharge records
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : completedTickets) {
            // Skip if patient already has a discharge record
            if (dischargeDAO.hasDischargeRecord(ticket.getPatient().getId())) {
                // If filtering by status, check the discharge status
                if (!"All Status".equals(statusFilter)) {
                    Discharge existingDischarge = dischargeDAO.findByPatientId(ticket.getPatient().getId());
                    if (existingDischarge != null) {
                        DischargeStatus filterStatus = mapStringToDischargeStatus(statusFilter);
                        if (existingDischarge.getStatus() == filterStatus) {
                            // Include this ticket if it matches the status filter
                            if (matchesSearch(ticket, searchTerm)) {
                                filteredTickets.add(ticket);
                            }
                        }
                    }
                } else {
                    // Include tickets with existing discharge records when showing all
                    if (matchesSearch(ticket, searchTerm)) {
                        filteredTickets.add(ticket);
                    }
                }
            } else {
                // No discharge record - show as "Pending Review"
                if ("All Status".equals(statusFilter) || "Pending Review".equals(statusFilter)) {
                    if (matchesSearch(ticket, searchTerm)) {
                        filteredTickets.add(ticket);
                    }
                }
            }
        }
        
        if (filteredTickets.isEmpty()) {
            Label emptyLabel = new Label("No completed tickets found");
            emptyLabel.getStyleClass().add("queue-empty-label");
            dischargeTableContainer.getChildren().add(emptyLabel);
        } else {
            for (Ticket ticket : filteredTickets) {
                GridPane row = createDischargeRow(ticket);
                dischargeTableContainer.getChildren().add(row);
            }
        }
    }
    
    /**
     * Checks if a ticket matches the search term
     */
    private boolean matchesSearch(Ticket ticket, String searchTerm) {
        if (searchTerm.isEmpty()) {
            return true;
        }
        
        Patient patient = ticket.getPatient();
        if (patient == null) {
            return false;
        }
        
        String patientId = patient.getId() != null ? patient.getId().toLowerCase() : "";
        String patientName = patient.getName() != null ? patient.getName().toLowerCase() : "";
        
        return patientId.contains(searchTerm) || patientName.contains(searchTerm);
    }
    
    /**
     * Creates a table row for a completed ticket (for discharge processing)
     */
    private GridPane createDischargeRow(Ticket ticket) {
        GridPane row = new GridPane();
        row.getStyleClass().add("table-data-row");
        
        // Column constraints (6 columns now)
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col1.setPercentWidth(12);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col2.setPercentWidth(20);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col3.setPercentWidth(18);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col4.setPercentWidth(18);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col5.setPercentWidth(17);
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        col6.setPercentWidth(15);
        
        row.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);
        
        Patient patient = ticket.getPatient();
        
        // Patient ID
        Label patientIdLabel = new Label(patient.getId());
        patientIdLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        GridPane.setColumnIndex(patientIdLabel, 0);
        row.getChildren().add(patientIdLabel);
        
        // Patient Name
        Label nameLabel = new Label(patient.getName());
        nameLabel.setStyle("-fx-text-fill: #555555;");
        GridPane.setColumnIndex(nameLabel, 1);
        row.getChildren().add(nameLabel);
        
        // Department (from doctor)
        String department = "N/A";
        if (ticket.getAssignedDoctorId() != null) {
            department = ticketDAO.getDoctorDepartment(ticket.getAssignedDoctorId());
            if (department == null || department.isEmpty()) {
                department = "N/A";
            }
        }
        Label deptLabel = new Label(department);
        deptLabel.setStyle("-fx-text-fill: #777777;");
        GridPane.setColumnIndex(deptLabel, 2);
        row.getChildren().add(deptLabel);
        
        // Assigned Doctor
        String doctorName = ticket.getAssignedDoctorName() != null ? ticket.getAssignedDoctorName() : "N/A";
        Label doctorLabel = new Label(doctorName);
        doctorLabel.setStyle("-fx-text-fill: #777777;");
        GridPane.setColumnIndex(doctorLabel, 3);
        row.getChildren().add(doctorLabel);
        
        // Status Badge (check if discharge record exists)
        Discharge existingDischarge = dischargeDAO.findByPatientId(patient.getId());
        DischargeStatus status;
        if (existingDischarge != null) {
            status = existingDischarge.getStatus();
        } else {
            status = DischargeStatus.PENDING_REVIEW; // No discharge record yet
        }
        
        Label statusLabel = new Label(getDischargeStatusDisplay(status));
        statusLabel.getStyleClass().add(getDischargeStatusStyleClass(status));
        GridPane.setColumnIndex(statusLabel, 4);
        row.getChildren().add(statusLabel);
        
        // Action Button
        Node actionNode;
        if (existingDischarge == null || existingDischarge.getStatus() == DischargeStatus.PENDING_REVIEW) {
            Button processBtn = new Button("Process");
            processBtn.getStyleClass().add("btn-action-process");
            processBtn.setOnAction(this::handleProcess);
            processBtn.setUserData(ticket); // Store ticket instead of discharge
            actionNode = processBtn;
        } else if (existingDischarge.getStatus() == DischargeStatus.READY) {
            Button dischargeBtn = new Button("Discharge");
            dischargeBtn.getStyleClass().add("btn-action-discharge");
            dischargeBtn.setOnAction(this::handleDischarge);
            dischargeBtn.setUserData(existingDischarge);
            actionNode = dischargeBtn;
        } else {
            Label noActionLabel = new Label("---");
            noActionLabel.setStyle("-fx-text-fill: #999;");
            actionNode = noActionLabel;
        }
        GridPane.setColumnIndex(actionNode, 5);
        row.getChildren().add(actionNode);
        
        return row;
    }
    
    /**
     * Maps string to DischargeStatus enum
     */
    private DischargeStatus mapStringToDischargeStatus(String statusStr) {
        switch (statusStr) {
            case "Ready": return DischargeStatus.READY;
            case "Pending Review": return DischargeStatus.PENDING_REVIEW;
            case "Cleared": return DischargeStatus.CLEARED;
            case "Discharged": return DischargeStatus.DISCHARGED;
            default: return null;
        }
    }
    
    /**
     * Gets display text for discharge status
     */
    private String getDischargeStatusDisplay(DischargeStatus status) {
        if (status == null) return "Unknown";
        switch (status) {
            case READY: return "Ready";
            case PENDING_REVIEW: return "Pending Review";
            case CLEARED: return "Cleared";
            case DISCHARGED: return "Discharged";
            default: return "Unknown";
        }
    }
    
    /**
     * Gets CSS style class for discharge status badge
     */
    private String getDischargeStatusStyleClass(DischargeStatus status) {
        if (status == null) return "badge-ready";
        switch (status) {
            case READY: return "badge-ready";
            case PENDING_REVIEW: return "badge-pending-review";
            case CLEARED: return "badge-cleared";
            case DISCHARGED: return "badge-cleared";
            default: return "badge-ready";
        }
    }
    
    /**
     * Populates the modal with patient and ticket information
     */
    private void populateModal(Ticket ticket) {
        populateModal(ticket, null);
    }
    
    /**
     * Populates the modal with patient and ticket information
     * @param ticket The ticket/visit
     * @param providedPrescription Optional prescription to use (if already retrieved)
     */
    private void populateModal(Ticket ticket, Prescription providedPrescription) {
        Patient patient = ticket.getPatient();
        
        // Use provided prescription if available, otherwise get from database
        Prescription prescription = providedPrescription;
        
        if (prescription == null) {
            // Get prescription for this specific visit (patient + assigned doctor)
            if (ticket.getAssignedDoctorId() != null && !ticket.getAssignedDoctorId().isEmpty()) {
                List<Prescription> prescriptions = prescriptionDAO.findByPatientAndDoctor(
                    patient.getId(), 
                    ticket.getAssignedDoctorId()
                );
                // Get the most recent prescription (first one since they're ordered DESC)
                if (!prescriptions.isEmpty()) {
                    prescription = prescriptions.get(0);
                    // Reload from database to ensure all fields are populated
                    if (prescription.getPrescriptionId() != null) {
                        Prescription reloaded = prescriptionDAO.findById(prescription.getPrescriptionId());
                        if (reloaded != null) {
                            prescription = reloaded;
                        }
                    }
                }
            }
            
            // Fallback: if no prescription found for this doctor, try to get latest for patient
            if (prescription == null) {
                List<Prescription> allPrescriptions = prescriptionDAO.findByPatient(patient.getId());
                if (!allPrescriptions.isEmpty()) {
                    prescription = allPrescriptions.get(0);
                    // Reload from database to ensure all fields are populated
                    if (prescription.getPrescriptionId() != null) {
                        Prescription reloaded = prescriptionDAO.findById(prescription.getPrescriptionId());
                        if (reloaded != null) {
                            prescription = reloaded;
                        }
                    }
                }
            }
        }
        
        // Populate patient information
        if (modalPatientName != null) {
            modalPatientName.setText(patient.getName());
        }
        if (modalPatientPhone != null) {
            modalPatientPhone.setText(patient.getContactNumber() != null ? patient.getContactNumber() : "N/A");
        }
        if (modalPatientAge != null) {
            modalPatientAge.setText(String.valueOf(patient.getAge()));
        }
        if (modalDoctorAssigned != null) {
            String doctorName = ticket.getAssignedDoctorName() != null ? ticket.getAssignedDoctorName() : "N/A";
            modalDoctorAssigned.setText(doctorName);
        }
        if (modalChiefComplaint != null) {
            // Get chief complaint from ticket service type or patient
            String chiefComplaint = ticket.getServiceType();
            if (chiefComplaint == null || chiefComplaint.isEmpty()) {
                chiefComplaint = patientDAO.getChiefComplaint(patient.getId());
                if (chiefComplaint == null || chiefComplaint.isEmpty()) {
                    chiefComplaint = patient.getNotes() != null ? patient.getNotes() : "N/A";
                }
            }
            modalChiefComplaint.setText(chiefComplaint);
        }
        
        // Populate consultation information
        if (modalPrescription != null) {
            if (prescription != null) {
                String medication = prescription.getMedication() != null ? prescription.getMedication() : "N/A";
                String dosage = prescription.getDosage() != null ? prescription.getDosage() : "";
                String frequency = prescription.getFrequency() != null ? prescription.getFrequency() : "";
                
                if (!dosage.isEmpty() && !frequency.isEmpty()) {
                    modalPrescription.setText(medication + " (" + dosage + ") - " + frequency);
                } else {
                    modalPrescription.setText(medication);
                }
            } else {
                modalPrescription.setText("No prescription available");
            }
        }
        if (modalConsultationDate != null) {
            if (prescription != null && prescription.getConsultationDate() != null) {
                modalConsultationDate.setText(prescription.getConsultationDate()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                modalConsultationDate.setText("N/A");
            }
        }
        if (modalConsultationNotes != null) {
            if (prescription != null) {
                String notes = prescription.getConsultationNotes();
                // Check if notes exist and are not empty
                if (notes != null && !notes.trim().isEmpty()) {
                    modalConsultationNotes.setText(notes);
                } else {
                    // If notes are null or empty, show a message
                    modalConsultationNotes.setText("No consultation notes available.");
                }
            } else {
                modalConsultationNotes.setText("No prescription found for this visit.");
            }
        }
    }
}