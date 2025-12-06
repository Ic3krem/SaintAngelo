package com.stangelo.saintangelo.utils;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;

/**
 * Service for playing announcement sounds and speaking ticket numbers
 * Uses Windows built-in TTS (free, no API key required)
 */
public class AnnouncementService {
    
    private static MediaPlayer announcementPlayer;
    
    /**
     * Plays the announce.mp3 file
     */
    public static void playAnnouncement() {
        try {
            String audioPath = AnnouncementService.class.getResource("/sound/announce.mp3").toString();
            Media media = new Media(audioPath);
            
            // Stop previous playback if any
            if (announcementPlayer != null) {
                announcementPlayer.stop();
            }
            
            announcementPlayer = new MediaPlayer(media);
            announcementPlayer.play();
        } catch (Exception e) {
            System.err.println("Error playing announcement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Speaks the ticket number using Windows TTS (free, no API key)
     * @param ticketNumber The ticket number to announce (e.g., "A5")
     */
    public static void speakTicketNumber(String ticketNumber) {
        try {
            // Format the message
            String message = "Now serving ticket number " + formatTicketForSpeech(ticketNumber);
            
            // Escape special characters for PowerShell
            message = message.replace("'", "''");
            
            // Use Windows built-in TTS via PowerShell (free, no API needed)
            String[] command = {
                "powershell",
                "-Command",
                "Add-Type -AssemblyName System.Speech; " +
                "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                "$speak.Speak('" + message + "');"
            };
            
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.start();
            
        } catch (IOException e) {
            System.err.println("Error speaking ticket number: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Formats ticket number for better speech (e.g., "A5" -> "A five")
     */
    private static String formatTicketForSpeech(String ticketNumber) {
        if (ticketNumber == null || ticketNumber.length() < 2) {
            return ticketNumber != null ? ticketNumber : "unknown";
        }
        
        char letter = ticketNumber.charAt(0);
        String number = ticketNumber.substring(1);
        
        // Convert single digit to word for clarity
        String numberWord = convertDigitToWord(number);
        
        return letter + " " + numberWord;
    }
    
    /**
     * Converts single digit to word (e.g., "5" -> "five")
     */
    private static String convertDigitToWord(String digit) {
        switch (digit) {
            case "1": return "one";
            case "2": return "two";
            case "3": return "three";
            case "4": return "four";
            case "5": return "five";
            case "6": return "six";
            case "7": return "seven";
            case "8": return "eight";
            case "9": return "nine";
            case "10": return "ten";
            default: return digit;
        }
    }
    
    /**
     * Plays announcement and then speaks ticket number
     * @param ticketNumber The ticket number to announce
     */
    public static void announceAndSpeak(String ticketNumber) {
        // Play the MP3 first
        playAnnouncement();
        
        // Wait for MP3 to finish, then speak
        if (announcementPlayer != null) {
            announcementPlayer.setOnEndOfMedia(() -> {
                // Small delay to ensure smooth transition
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                speakTicketNumber(ticketNumber);
            });
        } else {
            // If MP3 fails, just speak
            speakTicketNumber(ticketNumber);
        }
    }
}

