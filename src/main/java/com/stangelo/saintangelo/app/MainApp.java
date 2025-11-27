package com.stangelo.saintangelo.app;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class MainApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // CHANGED: Use TRANSPARENT instead of UNDECORATED to allow for rounded corners
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));

        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(10, 5, 5, 10));

        // CHANGED: Added -fx-background-radius: 30 30 0 0; to round only the top corners
        titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 30 30 0 0;");

        Label titleLabel = new Label("Saint Angelo Medical Center");
        titleLabel.setTextFill(Color.WHITE);

        // Pane to push buttons to the right
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Container for control buttons
        HBox controlButtons = new HBox(10);
        controlButtons.setAlignment(Pos.CENTER);

        // Minimize Button
        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().addAll("title-bar-button", "minimize-button");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));

        // Close Button
        Button closeButton = new Button("X");
        closeButton.getStyleClass().addAll("title-bar-button", "close-button");
        closeButton.setOnAction(event -> primaryStage.close());

        controlButtons.getChildren().addAll(minimizeButton, closeButton);

        titleBar.getChildren().addAll(titleLabel, spacer, controlButtons);
        titleBar.setPadding(new Insets(10, 40, 10, 10));

        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        BorderPane root = new BorderPane();

        // CHANGED: Set root background to transparent
        root.setStyle("-fx-background-color: transparent;");

        root.setTop(titleBar);
        root.setCenter(fxmlRoot);

        // --- ANIMATION SETUP START ---
        // 1. Initial State: Invisible and slightly smaller
        root.setOpacity(0);
        root.setScaleX(0.95);
        root.setScaleY(0.95);
        // --- ANIMATION SETUP END ---

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());

        // CHANGED: Set scene fill to transparent so the background styling shows through
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // --- PLAY GRANDIOSE ANIMATION ---
        playEntranceAnimation(root);
    }

    private void playEntranceAnimation(BorderPane root) {
        // 1. Fade In (1.5 seconds)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1500), root);
        fadeOut.setFromValue(0.0);
        fadeOut.setToValue(1.0);

        // 2. Scale Up / Zoom In (1.5 seconds)
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1500), root);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        // 3. Play both together
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(fadeOut, scaleUp);
        parallelTransition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}