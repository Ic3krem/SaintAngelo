package com.stangelo.saintangelo.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.stangelo.saintangelo.dao.DoctorDAO;
import com.stangelo.saintangelo.dao.PatientDAO;
import com.stangelo.saintangelo.dao.PrescriptionDAO;
import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.models.Doctor;
import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Prescription;
import com.stangelo.saintangelo.models.PriorityLevel;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.models.TicketStatus;
import com.stangelo.saintangelo.services.AuthService;
import com.stangelo.saintangelo.services.QueueManager;
import com.stangelo.saintangelo.services.QueueService;
import com.stangelo.saintangelo.utils.AnnouncementService;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MedicalDashboardController implements Initializable {

    // Stats Labels
    @FXML private Label totalTodayLabel;
    @FXML private Label waitingLabel;
    @FXML private Label avgWaitTimeLabel;
    
    // Stats Charts
    @FXML private LineChart<String, Number> totalTodayChart;
    @FXML private LineChart<String, Number> waitingChart;
    @FXML private LineChart<String, Number> avgWaitTimeChart;
    
    // Stats Footers
    @FXML private Label totalTodayFooter;
    @FXML private Label waitingFooter;
    @FXML private Label avgWaitTimeFooter;

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
    
    // Patient Selection ComboBox
    @FXML private ComboBox<Ticket> currentPatientsComboBox;

    // Navigation Buttons
    @FXML private Button btnCurrentPatient;
    @FXML private Button btnQueue;
    @FXML private Button btnRecords;
    @FXML private Button btnLogout;
    
    // Queue Management Tab Containers
    @FXML private TabPane queueTabPane;
    @FXML private VBox waitingQueueContainer;
    @FXML private VBox inProgressContainer;
    @FXML private VBox completedContainer;
    
    // DAOs
    private TicketDAO ticketDAO;
    private PatientDAO patientDAO;
    private PrescriptionDAO prescriptionDAO;
    private DoctorDAO doctorDAO;
    
    // Current ticket being served by this doctor
    private Ticket currentTicket;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ticketDAO = new TicketDAO();
        patientDAO = new PatientDAO();
        prescriptionDAO = new PrescriptionDAO();
        doctorDAO = new DoctorDAO();
        
        // Initialize ComboBox cell factory for patient display
        if (currentPatientsComboBox != null) {
            // Set prompt text
            currentPatientsComboBox.setPromptText("Select a patient");
            
            // Set StringConverter to properly display patient names in the button
            currentPatientsComboBox.setConverter(new javafx.util.StringConverter<Ticket>() {
                @Override
                public String toString(Ticket ticket) {
                    if (ticket == null || ticket.getPatient() == null) {
                        return "Select a patient";
                    }
                    String patientName = ticket.getPatient().getName();
                    return patientName != null && !patientName.trim().isEmpty() ? patientName : "Select a patient";
                }
                
                @Override
                public Ticket fromString(String string) {
                    // Not needed for display-only ComboBox
                    return null;
                }
            });
            
            // Cell factory for dropdown items
            currentPatientsComboBox.setCellFactory(param -> new ListCell<Ticket>() {
                @Override
                protected void updateItem(Ticket ticket, boolean empty) {
                    super.updateItem(ticket, empty);
                    if (empty || ticket == null || ticket.getPatient() == null) {
                        setText(null);
                    } else {
                        String ticketNum = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "N/A";
                        String patientName = ticket.getPatient().getName();
                        setText(ticketNum + " - " + patientName);
                    }
                }
            });
            
            // Button cell for displaying selected value
            currentPatientsComboBox.setButtonCell(new ListCell<Ticket>() {
                @Override
                protected void updateItem(Ticket ticket, boolean empty) {
                    super.updateItem(ticket, empty);
                    if (empty || ticket == null) {
                        setText("Select a patient");
                    } else if (ticket.getPatient() == null) {
                        setText("Select a patient");
                    } else {
                        String patientName = ticket.getPatient().getName();
                        setText(patientName != null && !patientName.trim().isEmpty() ? patientName : "Select a patient");
                    }
                }
            });
            
        }
        
        // Load dashboard data and current patient (sync happens inside loadDashboardData)
        loadDashboardData();
        loadCurrentPatient();
        loadCurrentPatientsComboBox();
        updateCharts();
        updateFooters();
        
        // Load queue management data if containers are available
        if (waitingQueueContainer != null || inProgressContainer != null || completedContainer != null) {
            loadQueueManagementData();
        }
    }

    private void loadDashboardData() {
        // Refresh all stats at once to ensure consistency
        QueueService.refreshDashboardStats();
    
        // Update Waiting count using shared QueueService method
        if (waitingLabel != null) {
            int waitingCount = QueueService.getWaitingCountForDashboard();
            waitingLabel.setText(String.valueOf(waitingCount));
        }
    
        // Update Total Today using shared QueueService method
        if (totalTodayLabel != null) {
            int totalToday = QueueService.getTotalTodayCount();
            totalTodayLabel.setText(String.valueOf(totalToday));
        }
    
        // Update Average Wait Time using shared QueueService method
        if (avgWaitTimeLabel != null) {
            int avgWaitTime = QueueService.getAverageWaitTimeToday();
            if (avgWaitTime > 0) {
                avgWaitTimeLabel.setText(avgWaitTime + " min");
            } else {
                avgWaitTimeLabel.setText("0 min");
            }
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
            updateLineChart(totalTodayChart, ticketDAO.getDailyTicketCountsLast7Days(), "#4ecdc4");
        }
        
        // Update Waiting Chart
        if (waitingChart != null) {
            updateLineChart(waitingChart, ticketDAO.getDailyWaitingCountsLast7Days(), "#96f233");
        }
        
        // Update Average Wait Time Chart
        if (avgWaitTimeChart != null) {
            updateLineChart(avgWaitTimeChart, ticketDAO.getDailyAverageWaitTimesLast7Days(), "#4ecdc4");
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
        
        // Update Waiting Footer
        if (waitingFooter != null) {
            double currentAvg = ticketDAO.getDailyWaitingCountsLast7Days().values().stream()
                .mapToInt(Integer::intValue).average().orElse(0.0);
            double previousAvg = ticketDAO.getAverageWaitingCountPrevious7Days();
            updateFooterLabel(waitingFooter, currentAvg, previousAvg);
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
     * Loads the current patient being served by this doctor
     */
    private void loadCurrentPatient() {
        // First, try to get ticket assigned to this specific doctor
        String doctorId = getDoctorId();
        Ticket doctorTicket = null;
        
        if (doctorId != null && !doctorId.isEmpty()) {
            // Check database directly for tickets assigned to this doctor
            doctorTicket = ticketDAO.findCurrentlyServingByDoctor(doctorId);
        }
        
        // If no ticket found for this doctor, try QueueService (general currently serving)
        if (doctorTicket == null) {
            doctorTicket = QueueService.getCurrentlyServingTicket();
            
            // Verify it's assigned to this doctor
            if (doctorTicket != null && doctorId != null) {
                if (!doctorId.equals(doctorTicket.getAssignedDoctorId())) {
                    // Ticket is assigned to a different doctor
                    doctorTicket = null;
                }
            }
        }
        
        currentTicket = doctorTicket;
        
        if (currentTicket != null && currentTicket.getPatient() != null) {
            Patient patient = currentTicket.getPatient();
            if(patientIdLabel != null) patientIdLabel.setText(patient.getId());
            if(patientNameLabel != null) patientNameLabel.setText(patient.getName());
            
            // Get chief complaint from ticket service type or patient notes
            String chiefComplaint = currentTicket.getServiceType();
            if (chiefComplaint == null || chiefComplaint.isEmpty()) {
                chiefComplaint = patient.getNotes() != null ? patient.getNotes() : "N/A";
            }
            if(chiefComplaintLabel != null) chiefComplaintLabel.setText(chiefComplaint);
            if(patientAgeLabel != null) patientAgeLabel.setText(String.valueOf(patient.getAge()));
            
            // Load existing prescription if available
            loadExistingPrescription(currentTicket);
            
            // Suggest treatments based on chief complaint
            suggestTreatmentBasedOnComplaint(chiefComplaint);
        } else {
            // No patient currently being served
            if(patientIdLabel != null) patientIdLabel.setText("---");
            if(patientNameLabel != null) patientNameLabel.setText("No patient");
            if(chiefComplaintLabel != null) chiefComplaintLabel.setText("---");
            if(patientAgeLabel != null) patientAgeLabel.setText("---");
            
            // Clear prescription fields
            if(medicationField != null) medicationField.clear();
            if(dosageField != null) dosageField.clear();
            if(frequencyField != null) frequencyField.clear();
            if(consultationNotesArea != null) consultationNotesArea.clear();
        }
    }
    
    /**
     * Loads all patients currently being treated (IN_SERVICE) by this doctor into the ComboBox
     */
    private void loadCurrentPatientsComboBox() {
        if (currentPatientsComboBox == null) return;
        
        String doctorId = getDoctorId();
        if (doctorId == null || doctorId.isEmpty()) {
            currentPatientsComboBox.getItems().clear();
            return;
        }
        
        // Get all IN_SERVICE tickets for this doctor
        List<Ticket> inServiceTickets = ticketDAO.findAllInServiceByDoctor(doctorId);
        
        // Update ComboBox items
        currentPatientsComboBox.getItems().clear();
        currentPatientsComboBox.getItems().addAll(inServiceTickets);
        
        // Select the current ticket if it exists
        if (currentTicket != null) {
            // Verify the ticket is still in the list
            if (inServiceTickets.contains(currentTicket)) {
                currentPatientsComboBox.setValue(currentTicket);
            } else {
                // Current ticket is no longer in service, select first available
                if (!inServiceTickets.isEmpty()) {
                    currentPatientsComboBox.setValue(inServiceTickets.get(0));
                    currentTicket = inServiceTickets.get(0);
                    // Load the newly selected patient
                    loadCurrentPatient();
                } else {
                    currentPatientsComboBox.setValue(null);
                }
            }
        } else if (!inServiceTickets.isEmpty()) {
            // Select the first one (most recent) and set as current
            currentPatientsComboBox.setValue(inServiceTickets.get(0));
            currentTicket = inServiceTickets.get(0);
            // Load the newly selected patient
            loadCurrentPatient();
        } else {
            // No patients in service, clear selection
            currentPatientsComboBox.setValue(null);
        }
    }
    
    /**
     * Handles patient selection from the ComboBox
     */
    @FXML
    private void handlePatientSelection(ActionEvent event) {
        if (currentPatientsComboBox == null) return;
        
        Ticket selectedTicket = currentPatientsComboBox.getValue();
        if (selectedTicket == null) return;
        
        // Switch to the selected patient
        currentTicket = selectedTicket;
        
        // Load patient information
        if (currentTicket.getPatient() != null) {
            Patient patient = currentTicket.getPatient();
            if(patientIdLabel != null) patientIdLabel.setText(patient.getId());
            if(patientNameLabel != null) patientNameLabel.setText(patient.getName());
            
            // Get chief complaint from ticket service type or patient notes
            String chiefComplaint = currentTicket.getServiceType();
            if (chiefComplaint == null || chiefComplaint.isEmpty()) {
                chiefComplaint = patient.getNotes() != null ? patient.getNotes() : "N/A";
            }
            if(chiefComplaintLabel != null) chiefComplaintLabel.setText(chiefComplaint);
            if(patientAgeLabel != null) patientAgeLabel.setText(String.valueOf(patient.getAge()));
            
            // Load existing prescription if available
            loadExistingPrescription(currentTicket);
            
            // Suggest treatments based on chief complaint
            suggestTreatmentBasedOnComplaint(chiefComplaint);
        }
    }
    
    /**
     * Loads existing prescription for the current ticket if available
     */
    private void loadExistingPrescription(Ticket ticket) {
        if (ticket == null || ticket.getPatient() == null) return;
        
        String doctorId = ticket.getAssignedDoctorId();
        if (doctorId == null || doctorId.isEmpty()) {
            doctorId = getDoctorId();
        }
        
        if (doctorId != null && !doctorId.isEmpty()) {
            List<Prescription> prescriptions = prescriptionDAO.findByPatientAndDoctor(
                ticket.getPatient().getId(), 
                doctorId
            );
            
            if (!prescriptions.isEmpty()) {
                // Get the most recent prescription
                Prescription prescription = prescriptions.get(0);
                
                if (medicationField != null) {
                    medicationField.setText(prescription.getMedication() != null ? prescription.getMedication() : "");
                }
                if (dosageField != null) {
                    dosageField.setText(prescription.getDosage() != null ? prescription.getDosage() : "");
                }
                if (frequencyField != null) {
                    frequencyField.setText(prescription.getFrequency() != null ? prescription.getFrequency() : "");
                }
                if (consultationNotesArea != null) {
                    consultationNotesArea.setText(prescription.getConsultationNotes() != null ? prescription.getConsultationNotes() : "");
                }
            } else {
                // Clear fields if no prescription found
                if (medicationField != null) medicationField.clear();
                if (dosageField != null) dosageField.clear();
                if (frequencyField != null) frequencyField.clear();
                if (consultationNotesArea != null) consultationNotesArea.clear();
            }
        }
    }
    
    /**
     * Suggests treatment/drugs based on chief complaint
     */
    private void suggestTreatmentBasedOnComplaint(String chiefComplaint) {
        if (chiefComplaint == null || chiefComplaint.isEmpty()) {
            return;
        }
        
        String complaintLower = chiefComplaint.toLowerCase();
        
        // Treatment suggestions based on common complaints
        // This is a simple mapping - in a real system, this would be more sophisticated
        String suggestedMedication = "";
        String suggestedDosage = "";
        String suggestedFrequency = "";
        
        if (complaintLower.contains("fever") || complaintLower.contains("temperature")) {
            suggestedMedication = "Paracetamol";
            suggestedDosage = "500 mg";
            suggestedFrequency = "3x daily for 5 days";
        } else if (complaintLower.contains("cough") || complaintLower.contains("cold")) {
            suggestedMedication = "Cough Syrup";
            suggestedDosage = "10 ml";
            suggestedFrequency = "3x daily";
        } else if (complaintLower.contains("headache") || complaintLower.contains("head")) {
            suggestedMedication = "Ibuprofen";
            suggestedDosage = "400 mg";
            suggestedFrequency = "2x daily as needed";
        } else if (complaintLower.contains("pain") || complaintLower.contains("ache")) {
            suggestedMedication = "Paracetamol";
            suggestedDosage = "500 mg";
            suggestedFrequency = "3x daily";
        } else if (complaintLower.contains("infection") || complaintLower.contains("bacterial")) {
            suggestedMedication = "Antibiotic (consult doctor)";
            suggestedDosage = "As prescribed";
            suggestedFrequency = "As prescribed";
        } else if (complaintLower.contains("allergy") || complaintLower.contains("allergic")) {
            suggestedMedication = "Antihistamine";
            suggestedDosage = "10 mg";
            suggestedFrequency = "Once daily";
        }
        
        // Only suggest if fields are empty (don't overwrite existing data)
        if (!suggestedMedication.isEmpty()) {
            if (medicationField != null && medicationField.getText().trim().isEmpty()) {
                medicationField.setText(suggestedMedication);
            }
            if (dosageField != null && dosageField.getText().trim().isEmpty()) {
                dosageField.setText(suggestedDosage);
            }
            if (frequencyField != null && frequencyField.getText().trim().isEmpty()) {
                frequencyField.setText(suggestedFrequency);
            }
        }
    }
    
    /**
     * Handles calling the next patient in the queue
     * Uses QueueService.dequeue() for proper queue operations
     */
    @FXML
    private void handleCallNextPatient(ActionEvent event) {
        // Get the current doctor's ID (from auth service or hardcoded for now)
        String doctorId = getDoctorId();
        
        if (doctorId == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Doctor ID not found. Please log in again.");
            return;
        }
        
        // Check if there are any incomplete patients (IN_SERVICE status)
        List<Ticket> inServiceTickets = ticketDAO.findAllInServiceByDoctor(doctorId);
        
        // If there are incomplete patients, show warning
        if (!inServiceTickets.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Incomplete Patients");
            warning.setHeaderText("You have " + inServiceTickets.size() + " patient(s) currently being treated.");
            warning.setContentText("Please complete treatment for all current patients before calling the next one.\n\n" +
                "You can switch between patients using the dropdown menu.");
            warning.showAndWait();
            return;
        }
        
        // If there's a current patient, complete their treatment first
        if (currentTicket != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Complete Current Patient");
            confirm.setHeaderText("You have a patient currently being served.");
            confirm.setContentText("Do you want to complete the current patient and call the next one?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
            
            // Complete current service using QueueService
            QueueService.completeCurrentService();
        }
        
        // Dequeue the next patient (proper queue operation)
        Ticket nextTicket = QueueService.dequeue(doctorId);
        
        if (nextTicket != null) {
            currentTicket = nextTicket;
            
            // Play announcement and speak ticket number
            String ticketNumber = nextTicket.getTicketNumber();
            if (ticketNumber != null) {
                AnnouncementService.announceAndSpeak(ticketNumber);
            }
            
            // Update UI
            loadCurrentPatient();
            loadCurrentPatientsComboBox(); // Refresh ComboBox with new patient
            loadDashboardData();
            updateCharts();
            updateFooters();
            
            showAlert(Alert.AlertType.INFORMATION, "Patient Called", 
                "Now serving: " + nextTicket.getTicketNumber() + "\n" +
                "Patient: " + (nextTicket.getPatient() != null ? nextTicket.getPatient().getName() : "Unknown"));
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Queue Empty", "No patients waiting in the queue.");
        }
    }
    
    /**
     * Gets the current doctor's ID
     */
    private String getDoctorId() {
        // Try to get from AuthService
        var currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            // The doctor ID might be different from user ID
            // For now, we'll use a hardcoded mapping or the user ID
            // In a real system, you'd have a doctors table linked to users
            return "DOC001"; // Default to first doctor for demo
        }
        return "DOC001"; // Fallback for demo
    }


    // --- TREATMENT HANDLERS ---

    @FXML
    private void handleCompleteTreatment() {
        // Validate required fields
        String medication = medicationField.getText().trim();
        String dosage = dosageField.getText().trim();
        String frequency = frequencyField.getText().trim();
        String consultationNotes = consultationNotesArea != null ? consultationNotesArea.getText().trim() : "";

        if (medication.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Information", "Please fill in all prescription details (medication, dosage, frequency).");
            return;
        }
        
        // Check if there's a current patient/ticket
        if (currentTicket == null || currentTicket.getPatient() == null) {
            showAlert(Alert.AlertType.WARNING, "No Patient", "No patient is currently being served. Please call a patient first.");
            return;
        }
        
        // Get doctor ID from ticket (assigned doctor) or fallback to current doctor
        String doctorId = currentTicket.getAssignedDoctorId();
        if (doctorId == null || doctorId.isEmpty()) {
            doctorId = getDoctorId();
        }
        
        if (doctorId == null || doctorId.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Doctor ID not found. Please log in again.");
            return;
        }
        
        Doctor doctor = doctorDAO.findById(doctorId);
        if (doctor == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Doctor information not found in database.");
            return;
        }
        
        // Create prescription
        String prescriptionId = "PR" + System.currentTimeMillis() % 100000;
        Patient patient = currentTicket.getPatient();
        
        Prescription prescription = new Prescription(
            prescriptionId,
            patient,
            doctor,
            medication,
            dosage,
            frequency,
            consultationNotes,
            java.time.LocalDateTime.now(), // consultation date
            null, // diagnosis - can be added later if needed
            null  // treatment plan - can be added later if needed
        );
        
        // Save prescription to database
        boolean saved = prescriptionDAO.create(prescription);
        
        if (saved) {
            // Mark ticket as completed
            boolean ticketUpdated = ticketDAO.updateStatus(currentTicket.getVisitId(), TicketStatus.COMPLETED);
            
            if (ticketUpdated) {
                // Complete the service in QueueService
                QueueService.completeCurrentService();
                
                // Clear form
                medicationField.clear();
                dosageField.clear();
                frequencyField.clear();
                if (consultationNotesArea != null) {
                    consultationNotesArea.clear();
                }
                
                // Clear current ticket
                currentTicket = null;
                
                // Reload UI
                loadCurrentPatient();
                loadCurrentPatientsComboBox(); // Refresh ComboBox to remove completed patient
                loadDashboardData();
                updateCharts();
                updateFooters();
                
                showAlert(Alert.AlertType.INFORMATION, "Treatment Complete", 
                    "Prescription saved successfully and patient marked as completed.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Partial Success", 
                    "Prescription saved, but failed to update ticket status. Please check manually.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save prescription. Please try again.");
        }
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
        // Refresh data after view loads
        javafx.application.Platform.runLater(() -> {
            QueueService.syncFromDatabase();
            loadDashboardData();
            loadCurrentPatient();
            updateCharts();
            updateFooters();
        });
    }

    @FXML
    private void handleNavQueue(ActionEvent event) {
        loadView(event, "/fxml/doctor-queue-management.fxml");
        // Refresh queue data after view loads
        javafx.application.Platform.runLater(() -> {
            loadQueueManagementData();
        });
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
            if (response == ButtonType.OK) {
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
            if (response == ButtonType.OK) {
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
            if (response == ButtonType.OK) {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}