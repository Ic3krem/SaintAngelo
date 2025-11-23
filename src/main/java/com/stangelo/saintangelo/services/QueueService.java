package com.stangelo.saintangelo.services;


import com.stangelo.saintangelo.models.Patient;
import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.models.TicketStatus;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class QueueService {

    // This IS the line of people.
    // LinkedList is perfect because we add to the back and remove from the front.
    private final LinkedList<Ticket> waitingQueue;

    // To keep track of people currently with doctors
    private final List<Ticket> activeTickets;

    private int dailyTicketCounter = 1; // Resets to 1 (A-001, A-002...)

    public QueueService() {
        this.waitingQueue = new LinkedList<>();
        this.activeTickets = new LinkedList<>();
    }

    /**
     * 1. REGISTER PATIENT
     * Creates a ticket and adds them to the line.
     */
    public Ticket registerPatient(Patient patient, String serviceType) {
        // Generate a simple ID like "A-001"
        String ticketNumber = String.format("A-%03d", dailyTicketCounter++);

        // Generate a unique Visit ID for the database
        String visitId = UUID.randomUUID().toString();

        // Create the ticket
        Ticket newTicket = new Ticket(visitId, ticketNumber, patient, TicketStatus.WAITING, LocalDateTime.now(), serviceType);

        // LOGIC: If Emergency or Senior, maybe add to FRONT of line?
        // For now, we just add to the back (Standard FIFO).
        waitingQueue.add(newTicket);

        System.out.println("Ticket Generated: " + ticketNumber + " for " + patient.getName());
        return newTicket;
    }

    /**
     * 2. CALL NEXT PATIENT
     * Removes the first person in line and sends them to the doctor.
     */
    public Ticket callNextTicket() {
        if (waitingQueue.isEmpty()) {
            System.out.println("Queue is empty!");
            return null;
        }

        // Remove from the FRONT of the line
        Ticket ticket = waitingQueue.poll();

        // Update status
        ticket.setStatus(TicketStatus.CALLED);

        // Add to active list (so we know they are currently being seen)
        activeTickets.add(ticket);

        System.out.println("Calling Ticket: " + ticket.getTicketNumber());
        return ticket;
    }

    /**
     * 3. GET INFO FOR DISPLAY
     * Use this for your TV Screen later.
     */
    public List<Ticket> getWaitingList() {
        return waitingQueue;
    }

    public Ticket getCurrentTicket() {
        if (activeTickets.isEmpty()) return null;
        return activeTickets.getLast(); // The most recently called
    }

    public void completeTicket(String visitId) {
        activeTickets.removeIf(ticket -> ticket.getVisitId().equals(visitId));
        // Optionally, you could add it to a 'completedTickets' list or log it
    }

    public List<Ticket> getActiveTickets() {
        return activeTickets;
    }

    public void resetDailyCounter() {
        this.dailyTicketCounter = 1;
    }



}
