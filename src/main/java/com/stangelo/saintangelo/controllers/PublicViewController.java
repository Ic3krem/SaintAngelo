package com.stangelo.saintangelo.controllers;

import com.stangelo.saintangelo.services.QueueService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    private static final int MAX_QUEUE_NUMBER = 260;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText(now.format(TIME_FORMATTER));
            dateLabel.setText(now.format(DATE_FORMATTER));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        if (nowServingNumberLabel != null) {
            nowServingNumberLabel.textProperty().bind(QueueService.queueNumberAsStringBinding());
        }

        QueueService.currentQueueNumberProperty().addListener((obs, oldVal, newVal) -> {
            updateNextInQueue(newVal.intValue());
        });

        updateNextInQueue(QueueService.getCurrentQueueNumber());
    }

    private void updateNextInQueue(int currentNumber) {
        nextQueueBox.getChildren().clear();
        for (int i = 1; i <= 5; i++) {
            // Calculate the next number with looping
            int nextNumber = (currentNumber + i -1) % MAX_QUEUE_NUMBER + 1;
            String formattedNumber = QueueService.formatQueueNumber(nextNumber);

            Label numberLabel = new Label(formattedNumber);
            numberLabel.getStyleClass().add("next-queue-number");

            VBox queueItem = new VBox(numberLabel);
            queueItem.getStyleClass().add("next-queue-item");
            queueItem.setAlignment(Pos.CENTER);
            HBox.setHgrow(queueItem, javafx.scene.layout.Priority.ALWAYS);

            nextQueueBox.getChildren().add(queueItem);
        }
    }
}
