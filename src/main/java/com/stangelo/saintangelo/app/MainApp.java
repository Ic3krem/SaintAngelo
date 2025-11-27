package com.stangelo.saintangelo.app;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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

        // This is the Login Screen Root
        BorderPane loginRoot = new BorderPane();
        loginRoot.setStyle("-fx-background-color: transparent;");
        loginRoot.setTop(titleBar);
        loginRoot.setCenter(fxmlRoot);

        // --- SPLASH SCREEN SETUP ---
        StackPane splashScreen = new StackPane();
        splashScreen.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 30;"); // Match app theme

        try {
            // Attempt to load the logo
            ImageView splashImage = new ImageView(new Image(getClass().getResourceAsStream("/images/cover2.png")));
            splashImage.setFitWidth(700);
            splashImage.setFitHeight(500);
            splashImage.setPreserveRatio(true);
            splashScreen.getChildren().add(splashImage);
        } catch (Exception e) {
            // Fallback text if image not found
            Label splashLabel = new Label("St. Angelo");
            splashLabel.setTextFill(Color.WHITE);
            splashLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Segoe UI';");
            splashScreen.getChildren().add(splashLabel);
        }

        // --- ROOT CONTAINER (Holds Splash + Login) ---
        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: transparent;");
        mainContainer.getChildren().addAll(loginRoot, splashScreen); // Splash is on top

        // --- PREPARE ANIMATION STATES ---
        // 1. Hide Login Screen initially
        loginRoot.setOpacity(0);
        loginRoot.setScaleX(0.95);
        loginRoot.setScaleY(0.95);

        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        // --- PLAY ANIMATION SEQUENCE ---
        playStartupSequence(splashScreen, loginRoot, mainContainer);
    }

    private void playStartupSequence(StackPane splashScreen, BorderPane loginRoot, StackPane mainContainer) {
        // 1. Wait for 2 seconds (Display Splash)
        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        // 2. Fade Out Splash Screen
        FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.8), splashScreen);
        fadeSplash.setFromValue(1.0);
        fadeSplash.setToValue(0.0);

        // 3. When Fade Out finishes, remove Splash and play Login Entrance
        fadeSplash.setOnFinished(e -> {
            mainContainer.getChildren().remove(splashScreen);
            playEntranceAnimation(loginRoot);
        });

        // Execute Sequence
        SequentialTransition sequence = new SequentialTransition(pause, fadeSplash);
        sequence.play();
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