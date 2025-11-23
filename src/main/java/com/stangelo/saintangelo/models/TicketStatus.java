package com.stangelo.saintangelo.models;

public enum TicketStatus {
    WAITING,    // Patient is sitting in the waiting room
    CALLED,     // Doctor has pressed "Call Next" (Blinking on screen)
    IN_SERVICE, // Patient is currently with the doctor
    COMPLETED,  // Doctor has finished the consultation
    SKIPPED     // Patient did not show up
}