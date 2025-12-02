package com.stangelo.saintangelo.services;

import java.util.List;

import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.models.Ticket;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * QueueService - Facade for queue operations
 * Uses QueueManager (PriorityQueue) internally for proper queue data structure
 * Provides JavaFX bindings for UI updates
 */
public class QueueService {
    private static final SimpleIntegerProperty currentQueueNumber = new SimpleIntegerProperty(0);
    private static final SimpleStringProperty lastGeneratedTicket = new SimpleStringProperty("---");
    private static final SimpleStringProperty lastPatientName = new SimpleStringProperty("");
    private static final int MAX_QUEUE_NUMBER = 260; // A1-A10, B1-B10, ... Z1-Z10 = 26 * 10 = 260

    // Observable properties for UI binding
    private static final SimpleObjectProperty<Ticket> currentlyServingTicket = new SimpleObjectProperty<>(null);
    private static final ObservableList<Ticket> waitingQueue = FXCollections.observableArrayList();
    
    // DAO for ticket number generation
    private static final TicketDAO ticketDAO = new TicketDAO();

    // =====================================================
    // PROPERTY ACCESSORS (for JavaFX binding)
    // =====================================================

    public static SimpleIntegerProperty currentQueueNumberProperty() {
        return currentQueueNumber;
    }

    public static SimpleStringProperty lastGeneratedTicketProperty() {
        return lastGeneratedTicket;
    }

    public static SimpleStringProperty lastPatientNameProperty() {
        return lastPatientName;
    }

    public static SimpleObjectProperty<Ticket> currentlyServingTicketProperty() {
        return currentlyServingTicket;
    }

    public static ObservableList<Ticket> getWaitingQueue() {
        return waitingQueue;
    }

    public static int getCurrentQueueNumber() {
        return currentQueueNumber.get();
    }

    public static String getLastGeneratedTicket() {
        return lastGeneratedTicket.get();
    }

    public static String getLastPatientName() {
        return lastPatientName.get();
    }

    public static Ticket getCurrentlyServingTicket() {
        return currentlyServingTicket.get();
    }

    // =====================================================
    // QUEUE OPERATIONS (using QueueManager)
    // =====================================================

    /**
     * Enqueues a ticket into the priority queue
     * @param ticket The ticket to add to the queue
     * @return true if successful
     */
    public static boolean enqueue(Ticket ticket) {
        boolean success = QueueManager.getInstance().enqueue(ticket);
        if (success) {
            refreshQueueData();
        }
        return success;
    }

    /**
     * Dequeues the next ticket (doctor calls next patient)
     * @param doctorId The doctor calling the patient
     * @return The dequeued ticket, or null if queue is empty
     */
    public static Ticket dequeue(String doctorId) {
        Ticket ticket = QueueManager.getInstance().dequeue(doctorId);
        refreshQueueData();
        return ticket;
    }

    /**
     * Peeks at the next ticket without removing it
     * @return The next ticket, or null if empty
     */
    public static Ticket peekNext() {
        return QueueManager.getInstance().peek();
    }

    /**
     * Completes the current service
     * @return true if successful
     */
    public static boolean completeCurrentService() {
        boolean success = QueueManager.getInstance().completeCurrentService();
        if (success) {
            refreshQueueData();
        }
        return success;
    }

    /**
     * Skips the current patient
     * @return true if successful
     */
    public static boolean skipCurrentPatient() {
        boolean success = QueueManager.getInstance().skipCurrentPatient();
        if (success) {
            refreshQueueData();
        }
        return success;
    }

    /**
     * Removes a ticket from the queue
     * @param visitId The visit ID to remove
     * @return true if successful
     */
    public static boolean removeFromQueue(String visitId) {
        boolean success = QueueManager.getInstance().removeFromQueue(visitId);
        if (success) {
            refreshQueueData();
        }
        return success;
    }

    /**
     * Gets the position of a ticket in the queue
     * @param visitId The visit ID to find
     * @return Position (1-based), or -1 if not found
     */
    public static int getQueuePosition(String visitId) {
        return QueueManager.getInstance().getPosition(visitId);
    }

    // =====================================================
    // REFRESH & SYNC
    // =====================================================

    /**
     * Refreshes the observable queue data from QueueManager
     * Updates UI bindings
     */
    public static void refreshQueueData() {
        QueueManager qm = QueueManager.getInstance();
        
        // Update currently serving
        currentlyServingTicket.set(qm.getCurrentlyServing());

        // Update waiting queue (top 5 for display)
        List<Ticket> waiting = qm.getWaitingList(5);
        waitingQueue.setAll(waiting);
    }

    /**
     * Syncs QueueManager with database
     * Call this on startup or when external changes may have occurred
     */
    public static void syncFromDatabase() {
        QueueManager.getInstance().syncFromDatabase();
        refreshQueueData();
    }

    // =====================================================
    // TICKET NUMBER GENERATION
    // =====================================================

    /**
     * Generates the next queue ticket number from database
     * Format: A1-A10, B1-B10, ... Z1-Z10
     * @param patientName Name of the patient for the ticket
     * @return The formatted ticket number (e.g., "A1", "B5", etc.)
     */
    public static String generateNextTicket(String patientName) {
        // Get next ticket number from database
        String ticketNumber = ticketDAO.getNextTicketNumber();
        
        // Update local state for UI
        lastGeneratedTicket.set(ticketNumber);
        lastPatientName.set(patientName != null ? patientName : "");
        
        // Update queue number for UI
        int queueNum = parseTicketNumber(ticketNumber);
        currentQueueNumber.set(queueNum);
        
        return ticketNumber;
    }

    /**
     * Parses a ticket number string (e.g., "A5") to its numeric value (e.g., 5)
     */
    private static int parseTicketNumber(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.length() < 2) {
            return 0;
        }
        try {
            char letter = ticketNumber.charAt(0);
            int number = Integer.parseInt(ticketNumber.substring(1));
            return (letter - 'A') * 10 + number;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static void incrementQueueNumber() {
        int current = currentQueueNumber.get();
        currentQueueNumber.set((current % MAX_QUEUE_NUMBER) + 1);
    }

    public static void resetQueue() {
        currentQueueNumber.set(0);
        lastGeneratedTicket.set("---");
        lastPatientName.set("");
        currentlyServingTicket.set(null);
        waitingQueue.clear();
        QueueManager.getInstance().reset();
    }

    /**
     * Formats a queue number to the letter-number format
     * 1-10 = A1-A10, 11-20 = B1-B10, etc.
     * @param n The queue number (1-260)
     * @return Formatted string like "A1", "B5", "Z10"
     */
    public static String formatQueueNumber(int n) {
        if (n <= 0 || n > MAX_QUEUE_NUMBER) {
            return "---";
        }
        char letter = (char) ('A' + (n - 1) / 10);
        int number = (n - 1) % 10 + 1;
        return "" + letter + number;
    }

    // =====================================================
    // JAVAFX BINDINGS
    // =====================================================

    public static StringBinding queueNumberAsStringBinding() {
        return Bindings.createStringBinding(() -> {
            int n = currentQueueNumber.get();
            if (n == 0) {
                return "---";
            }
            return formatQueueNumber(n);
        }, currentQueueNumber);
    }

    public static StringBinding lastTicketAsStringBinding() {
        return Bindings.createStringBinding(() -> lastGeneratedTicket.get(), lastGeneratedTicket);
    }

    public static StringBinding currentlyServingNumberBinding() {
        return Bindings.createStringBinding(() -> {
            Ticket ticket = currentlyServingTicket.get();
            return ticket != null ? ticket.getTicketNumber() : "---";
        }, currentlyServingTicket);
    }

    public static StringBinding currentlyServingPatientBinding() {
        return Bindings.createStringBinding(() -> {
            Ticket ticket = currentlyServingTicket.get();
            return ticket != null && ticket.getPatient() != null ? ticket.getPatient().getName() : "";
        }, currentlyServingTicket);
    }

    public static StringBinding assignedDoctorBinding() {
        return Bindings.createStringBinding(() -> {
            Ticket ticket = currentlyServingTicket.get();
            if (ticket == null) {
                return "Not Yet Assigned";
            }
            return ticket.hasAssignedDoctor() ? ticket.getAssignedDoctorName() : "Not Yet Assigned";
        }, currentlyServingTicket);
    }

    /**
     * Gets the count of waiting patients
     */
    public static int getWaitingCount() {
        return QueueManager.getInstance().size();
    }
}
