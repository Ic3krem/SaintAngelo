package com.stangelo.saintangelo;

import javafx.animation.*;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;

public class TestApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Crucial for achieving background transparency for the Stage/Window
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));

        // --- TITLE BAR SETUP (Remains the same) ---
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        // Padding: Top, Right, Bottom, Left
        titleBar.setPadding(new Insets(5, 5, 10, 10));
        // Note: Title bar *must* have a background color for visibility/dragging
        titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 0;");

        // 1. Window Icon
        ImageView windowIcon = new ImageView();
        try {
            windowIcon.setImage(new Image(getClass().getResourceAsStream("/images/cover2.png")));
            windowIcon.setFitWidth(18);
            windowIcon.setFitHeight(18);
            windowIcon.setPreserveRatio(true);
        } catch (Exception e) {
            // Fallback if image fails
        }

        // 2. Title Label
        Label titleLabel = new Label("Saint Angelo Medical Center");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Group Icon and Text
        HBox titleContent = new HBox(10); // 10px spacing between icon and text
        titleContent.setAlignment(Pos.CENTER_LEFT);
        titleContent.getChildren().addAll(windowIcon, titleLabel);

        // 3. Spacer (Pushes buttons to the right)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Window Controls
        Button minimizeButton = new Button("—");
        minimizeButton.getStyleClass().addAll("title-bar-button");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("title-bar-button", "close-button");
        closeButton.setOnAction(event -> primaryStage.close());

        HBox windowControls = new HBox(5);
        windowControls.setAlignment(Pos.CENTER_RIGHT);
        windowControls.getChildren().addAll(minimizeButton, closeButton);

        // Add all to Title Bar
        titleBar.getChildren().addAll(titleContent, spacer, windowControls);

        // 5. Drag Functionality
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        // --- MAIN LAYOUT ---
        BorderPane loginRoot = new BorderPane();
        loginRoot.setStyle("-fx-background-radius: 0;");
        loginRoot.setTop(titleBar);
        loginRoot.setCenter(fxmlRoot);

        // Clip the BorderPane to ensure content doesn't bleed out of rounded corners
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(loginRoot.widthProperty());
        clip.heightProperty().bind(loginRoot.heightProperty());
        clip.setArcWidth(0);
        clip.setArcHeight(0);
        loginRoot.setClip(clip);

        // --- SPLASH SCREEN ---
        StackPane splashScreen = new StackPane();
        // ** THE FIX: Set background to transparent **
        // This ensures only the logo is visible, and the stage transparency shows through.
        splashScreen.setStyle("-fx-background-color: transparent;");

        try {
            ImageView splashImage = new ImageView(new Image(getClass().getResourceAsStream("/images/cover2.png")));
            // You might want to make the initial splash screen smaller
            splashImage.setFitWidth(300); // Smaller size for logo only effect
            splashImage.setFitHeight(300);
            splashImage.setPreserveRatio(true);
            splashScreen.getChildren().add(splashImage);
        } catch (Exception e) {
            Label splashLabel = new Label("St. Angelo");
            splashLabel.setTextFill(Color.web("#007345"));
            splashLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
            splashScreen.getChildren().add(splashLabel);
        }

        StackPane mainContainer = new StackPane();
        mainContainer.setStyle("-fx-background-color: transparent;");
        mainContainer.getChildren().addAll(loginRoot, splashScreen);

        // Animation Initial State
        loginRoot.setOpacity(0);
        loginRoot.setScaleX(0.95);
        loginRoot.setScaleY(0.95);

        Scene scene = new Scene(mainContainer);
        scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());
        // Crucial for achieving background transparency for the Scene
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.show();

        playStartupSequence(splashScreen, loginRoot, mainContainer);
    }

    private void playStartupSequence(StackPane splashScreen, BorderPane loginRoot, StackPane mainContainer) {
        // Added a short delay (e.g., 0.5s) to allow the transparent window to display the logo clearly
        PauseTransition initialDelay = new PauseTransition(Duration.seconds(0.5));

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5)); // Total time on screen ~ 2.0s
        FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.8), splashScreen);
        fadeSplash.setFromValue(1.0);
        fadeSplash.setToValue(0.0);
        fadeSplash.setOnFinished(e -> {
            mainContainer.getChildren().remove(splashScreen);
            playEntranceAnimation(loginRoot);
        });

        // Sequence: Initial Delay -> Pause (Wait for user to see logo) -> Fade Out
        SequentialTransition sequence = new SequentialTransition(initialDelay, pause, fadeSplash);
        sequence.play();
    }

    private void playEntranceAnimation(BorderPane root) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(1500), root);
        fadeOut.setFromValue(0.0);
        fadeOut.setToValue(1.0);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1500), root);
        scaleUp.setFromX(0.95);
        scaleUp.setFromY(0.95);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(fadeOut, scaleUp);
        parallelTransition.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}