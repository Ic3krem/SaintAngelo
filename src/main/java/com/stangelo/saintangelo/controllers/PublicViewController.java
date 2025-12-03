package com.stangelo.saintangelo.controllers;

import com.stangelo.saintangelo.models.Ticket;
import com.stangelo.saintangelo.services.QueueService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PublicViewController implements Initializable {

    @FXML
    private Label timeLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label nowServingNumberLabel;

    @FXML
    private HBox nextQueueBox;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    
    private Timeline syncTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize clock for time and date
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(TIME_FORMATTER));
            dateLabel.setText(now.format(DATE_FORMATTER));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Bind "Now Serving" to the actual currently serving ticket from database
        if (nowServingNumberLabel != null) {
            nowServingNumberLabel.textProperty().bind(QueueService.currentlyServingNumberBinding());
        }

        // Listen to currently serving ticket changes
        QueueService.currentlyServingTicketProperty().addListener((obs, oldVal, newVal) -> {
            updateNextInQueue();
        });

        // Listen to waiting queue changes
        QueueService.getWaitingQueue().addListener((ListChangeListener<Ticket>) change -> {
            updateNextInQueue();
        });

        // Initial sync from database
        QueueService.syncFromDatabase();
        updateNextInQueue();

        // Set up periodic database sync (every 3 seconds - reduced frequency for better network stability)
        syncTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            try {
                QueueService.syncFromDatabase();
                updateNextInQueue();
            } catch (Exception ex) {
                // Silently handle sync errors - UI will show last known state
                // Errors are already logged in QueueManager
            }
        }));
        syncTimeline.setCycleCount(Animation.INDEFINITE);
        syncTimeline.play();
    }

    /**
     * Updates the "Next in Queue" section with actual waiting tickets from the database
     */
    private void updateNextInQueue() {
        nextQueueBox.getChildren().clear();
        
        // Get the actual next 5 waiting tickets (priority-ordered)
        ObservableList<Ticket> waitingTickets = QueueService.getWaitingQueue();
        
        int count = 0;
        for (Ticket ticket : waitingTickets) {
            if (count >= 5) break; // Only show first 5
            
            String ticketNumber = ticket.getTicketNumber() != null ? ticket.getTicketNumber() : "---";
            
            Label numberLabel = new Label(ticketNumber);
            numberLabel.getStyleClass().add("next-queue-number");

            VBox queueItem = new VBox(numberLabel);
            queueItem.getStyleClass().add("next-queue-item");
            queueItem.setAlignment(Pos.CENTER);
            HBox.setHgrow(queueItem, javafx.scene.layout.Priority.ALWAYS);

            nextQueueBox.getChildren().add(queueItem);
            count++;
        }
        
        // Fill remaining slots with "---" if less than 5 tickets
        while (count < 5) {
            Label numberLabel = new Label("---");
            numberLabel.getStyleClass().add("next-queue-number");

            VBox queueItem = new VBox(numberLabel);
            queueItem.getStyleClass().add("next-queue-item");
            queueItem.setAlignment(Pos.CENTER);
            HBox.setHgrow(queueItem, javafx.scene.layout.Priority.ALWAYS);

            nextQueueBox.getChildren().add(queueItem);
            count++;
        }
    }
}
