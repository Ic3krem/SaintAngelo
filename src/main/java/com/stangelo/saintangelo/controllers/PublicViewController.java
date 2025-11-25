package com.stangelo.saintangelo.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PublicViewController {

    @FXML
    private Label timeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label nowServingNumberLabel;

    @FXML
    private HBox nextQueueBox;

    // Formatters for the time and date
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

    @FXML
    public void initialize() {
        // Start a timeline to update the time and date every second
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(TIME_FORMATTER));
            dateLabel.setText(now.format(DATE_FORMATTER));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // In a real application, you would fetch the queue data from your QueueService here
        // and update the labels accordingly.
        // For now, we'll just use the placeholder data from the FXML.
    }

    /**
     * This method would be called by a service to update the "Now Serving" display.
     * @param ticketNumber The new ticket number to display.
     */
    public void updateNowServing(String ticketNumber) {
        nowServingNumberLabel.setText(ticketNumber);
    }

    /**
     * This method would be called by a service to update the "Next in Queue" list.
     * @param nextTickets A list of the next ticket numbers.
     */
    public void updateNextInQueue(List<String> nextTickets) {
        nextQueueBox.getChildren().clear(); // Clear the existing items
        for (String ticketNumber : nextTickets) {
            // You would create a new VBox with the correct style classes and add it here
            // For simplicity, this part is left as an exercise.
        }
    }
}
