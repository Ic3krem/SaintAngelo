package com.stangelo.saintangelo.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.stangelo.saintangelo.dao.TicketDAO;
import com.stangelo.saintangelo.models.PriorityLevel;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.models.TicketStatus;

/**
 * Queue Manager using PriorityQueue data structure
 * Syncs with database for persistence
 * 
 * Priority Order: EMERGENCY > SENIOR_CITIZEN > REGULAR
 * Within same priority: First-Come-First-Served (by creation time)
 */
public class QueueManager {
    
    private static final Logger logger = Logger.getLogger(QueueManager.class.getName());
    
    // Singleton instance
    private static QueueManager instance;
    
    // The priority queue - orders by priority level, then by creation time
    private final PriorityQueue<Ticket> waitingQueue;
    
    // Currently serving ticket
    private Ticket currentlyServing;
    
    // DAO for database operations
    private final TicketDAO ticketDAO;
    
    /**
     * Comparator for ticket priority ordering
     * Emergency (1) > Senior Citizen (2) > Regular (3)
     * Within same priority, earlier creation time comes first
     */
    private static final Comparator<Ticket> TICKET_COMPARATOR = (t1, t2) -> {
        // First compare by priority
        int priorityCompare = getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority());
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // If same priority, compare by creation time (earlier first)
        return t1.getCreatedTime().compareTo(t2.getCreatedTime());
    };
    
    /**
     * Gets numeric value for priority (lower = higher priority)
     */
    private static int getPriorityValue(PriorityLevel priority) {
        if (priority == null) return 3;
        switch (priority) {
            case EMERGENCY: return 1;
            case SENIOR_CITIZEN: return 2;
            case REGULAR: 
            default: return 3;
        }
    }
    
    /**
     * Private constructor for singleton
     */
    private QueueManager() {
        this.ticketDAO = new TicketDAO();
        this.waitingQueue = new PriorityQueue<>(TICKET_COMPARATOR);
        this.currentlyServing = null;
        
        // Load initial data from database
        syncFromDatabase();
    }
    
    /**
     * Gets the singleton instance
     */
    public static synchronized QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }
    
    /**
     * Syncs the in-memory queue with the database
     * Called on startup and when needed
     * Only loads today's waiting tickets to match countWaitingTickets() behavior
     * Handles connection failures gracefully with automatic reconnection
     */
    public synchronized void syncFromDatabase() {
        logger.info("Syncing queue from database...");
        
        try {
            // Clear current queue
            waitingQueue.clear();
            
            // Load waiting tickets from database (only today's tickets to match countWaitingTickets())
            List<Ticket> waitingTickets = ticketDAO.findWaitingTickets(Integer.MAX_VALUE);
            logger.info("Found " + waitingTickets.size() + " waiting tickets in database");
            
            for (Ticket ticket : waitingTickets) {
                if (ticket != null && ticket.getVisitId() != null) {
                    waitingQueue.offer(ticket);
                    logger.fine("Added ticket to queue: " + ticket.getTicketNumber() + " (Priority: " + ticket.getPriority() + ")");
                } else {
                    logger.warning("Skipping null or invalid ticket during sync");
                }
            }
            
            // Load currently serving ticket
            currentlyServing = ticketDAO.findCurrentlyServing();
            
            logger.info("Queue synced successfully. Waiting: " + waitingQueue.size() + 
                       ", Currently serving: " + (currentlyServing != null ? currentlyServing.getTicketNumber() : "none"));
        } catch (Exception e) {
            // Handle connection failures and other database errors
            logger.warning("Error syncing from database: " + e.getMessage());
            logger.log(Level.WARNING, "Exception details: ", e);
            
            // Try to reset connection and retry once
            try {
                com.stangelo.saintangelo.utils.DatabaseConnection.resetConnection();
                logger.info("Connection reset, retrying sync...");
                
                // Retry the sync
                waitingQueue.clear();
                List<Ticket> waitingTickets = ticketDAO.findWaitingTickets(Integer.MAX_VALUE);
                logger.info("Retry: Found " + waitingTickets.size() + " waiting tickets in database");
                
                for (Ticket ticket : waitingTickets) {
                    if (ticket != null && ticket.getVisitId() != null) {
                        waitingQueue.offer(ticket);
                    }
                }
                currentlyServing = ticketDAO.findCurrentlyServing();
                
                logger.info("Queue synced after reconnection. Waiting: " + waitingQueue.size() + 
                           ", Currently serving: " + (currentlyServing != null ? currentlyServing.getTicketNumber() : "none"));
            } catch (Exception retryException) {
                logger.severe("Failed to sync after reconnection attempt: " + retryException.getMessage());
                logger.log(Level.SEVERE, "Retry exception details: ", retryException);
                // Keep existing queue data on failure - don't clear it
                // This ensures the UI still shows something even if sync fails
            }
        }
    }
    
    /**
     * Enqueues a new ticket (adds to queue)
     * Also persists to database
     * 
     * @param ticket The ticket to enqueue
     * @return true if successful
     */
    public synchronized boolean enqueue(Ticket ticket) {
        if (ticket == null) {
            logger.warning("Cannot enqueue null ticket");
            return false;
        }
        
        // Ensure ticket status is WAITING
        ticket.setStatus(TicketStatus.WAITING);
        
        // Save to database first
        boolean saved = ticketDAO.create(ticket);
        
        if (saved) {
            // Add to in-memory queue
            waitingQueue.offer(ticket);
            logger.info("Enqueued ticket: " + ticket.getTicketNumber() + 
                       " | Patient: " + (ticket.getPatient() != null ? ticket.getPatient().getName() : "Unknown") +
                       " | Priority: " + ticket.getPriority() +
                       " | Queue size: " + waitingQueue.size());
            return true;
        } else {
            logger.severe("Failed to save ticket to database: " + ticket.getTicketNumber());
            return false;
        }
    }
    
    /**
     * Dequeues the next ticket (removes from queue and marks as IN_SERVICE)
     * Called when a doctor calls the next patient
     * 
     * @param doctorId The doctor calling the patient
     * @return The dequeued ticket, or null if queue is empty
     */
    public synchronized Ticket dequeue(String doctorId) {
        if (waitingQueue.isEmpty()) {
            logger.info("Queue is empty, nothing to dequeue");
            return null;
        }
        
        // Get the highest priority ticket
        Ticket ticket = waitingQueue.poll();
        
        if (ticket != null) {
            // Update in database - assign doctor and change status
            boolean updated = ticketDAO.assignDoctor(ticket.getVisitId(), doctorId);
            
            if (updated) {
                // Refresh ticket data from database to get doctor name
                ticket = ticketDAO.findByVisitId(ticket.getVisitId());
                currentlyServing = ticket;
                
                logger.info("Dequeued ticket: " + ticket.getTicketNumber() + 
                           " | Assigned to doctor: " + doctorId +
                           " | Remaining in queue: " + waitingQueue.size());
                return ticket;
            } else {
                // Failed to update database, put ticket back
                waitingQueue.offer(ticket);
                logger.severe("Failed to update ticket in database: " + ticket.getTicketNumber());
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Peeks at the next ticket without removing it
     * 
     * @return The next ticket in queue, or null if empty
     */
    public synchronized Ticket peek() {
        return waitingQueue.peek();
    }
    
    /**
     * Gets the current queue size
     * 
     * @return Number of waiting tickets
     */
    public synchronized int size() {
        return waitingQueue.size();
    }
    
    /**
     * Checks if queue is empty
     * 
     * @return true if no waiting tickets
     */
    public synchronized boolean isEmpty() {
        return waitingQueue.isEmpty();
    }
    
    /**
     * Gets the currently serving ticket
     * 
     * @return The ticket currently being served, or null
     */
    public synchronized Ticket getCurrentlyServing() {
        return currentlyServing;
    }
    
    /**
     * Completes the current service (marks ticket as COMPLETED)
     * 
     * @return true if successful
     */
    public synchronized boolean completeCurrentService() {
        if (currentlyServing == null) {
            logger.warning("No ticket currently being served");
            return false;
        }
        
        boolean updated = ticketDAO.updateStatus(currentlyServing.getVisitId(), TicketStatus.COMPLETED);
        
        if (updated) {
            logger.info("Completed service for ticket: " + currentlyServing.getTicketNumber());
            currentlyServing = null;
            return true;
        }
        
        return false;
    }
    
    /**
     * Skips the current patient (marks ticket as SKIPPED)
     * 
     * @return true if successful
     */
    public synchronized boolean skipCurrentPatient() {
        if (currentlyServing == null) {
            logger.warning("No ticket currently being served to skip");
            return false;
        }
        
        boolean updated = ticketDAO.updateStatus(currentlyServing.getVisitId(), TicketStatus.SKIPPED);
        
        if (updated) {
            logger.info("Skipped ticket: " + currentlyServing.getTicketNumber());
            currentlyServing = null;
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets a list view of the waiting queue (for display purposes)
     * Returns tickets in priority order
     * 
     * @param limit Maximum number of tickets to return
     * @return List of waiting tickets in priority order
     */
    public synchronized List<Ticket> getWaitingList(int limit) {
        List<Ticket> result = new ArrayList<>();
        
        // Create a copy of the queue to iterate without modifying
        PriorityQueue<Ticket> tempQueue = new PriorityQueue<>(TICKET_COMPARATOR);
        tempQueue.addAll(waitingQueue);
        
        int count = 0;
        while (!tempQueue.isEmpty() && count < limit) {
            result.add(tempQueue.poll());
            count++;
        }
        
        return result;
    }
    
    /**
     * Gets all waiting tickets as a list
     * 
     * @return List of all waiting tickets in priority order
     */
    public synchronized List<Ticket> getAllWaiting() {
        return getWaitingList(Integer.MAX_VALUE);
    }
    
    /**
     * Removes a specific ticket from the queue (e.g., patient left)
     * 
     * @param visitId The visit ID of the ticket to remove
     * @return true if ticket was found and removed
     */
    public synchronized boolean removeFromQueue(String visitId) {
        Ticket toRemove = null;
        
        for (Ticket ticket : waitingQueue) {
            if (ticket.getVisitId().equals(visitId)) {
                toRemove = ticket;
                break;
            }
        }
        
        if (toRemove != null) {
            waitingQueue.remove(toRemove);
            ticketDAO.updateStatus(visitId, TicketStatus.SKIPPED);
            logger.info("Removed ticket from queue: " + toRemove.getTicketNumber());
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the position of a ticket in the queue
     * 
     * @param visitId The visit ID to find
     * @return Position (1-based), or -1 if not found
     */
    public synchronized int getPosition(String visitId) {
        List<Ticket> orderedList = getAllWaiting();
        
        for (int i = 0; i < orderedList.size(); i++) {
            if (orderedList.get(i).getVisitId().equals(visitId)) {
                return i + 1;
            }
        }
        
        return -1;
    }
    
    /**
     * Resets the queue manager (for testing or end of day)
     */
    public synchronized void reset() {
        waitingQueue.clear();
        currentlyServing = null;
        logger.info("Queue manager reset");
    }
}

