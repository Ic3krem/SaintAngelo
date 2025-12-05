package com.stangelo.saintangelo.app;

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

public class MainApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));

        // --- TITLE BAR SETUP ---
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        // Padding: Top, Right, Bottom, Left
        titleBar.setPadding(new Insets(5, 5, 10, 10));
        titleBar.setStyle("-fx-background-color: #007345; -fx-background-radius: 0 0 0 0;");

        // 1. Window Icon (Optional: Use your logo here)
        ImageView windowIcon = new ImageView();
        try {
            // Reusing your cover image or a specific icon file.
            // Ideally, create a 16x16 or 32x32 version of your logo.
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
        Button minimizeButton = new Button("—"); // Em dash looks cleaner
        minimizeButton.getStyleClass().addAll("title-bar-button");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));

        Button closeButton = new Button("✕"); // Multiplication X looks cleaner
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
        // Important: Set rounded corners on the bottom for the root as well to match
        loginRoot.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");
        loginRoot.setTop(titleBar);
        loginRoot.setCenter(fxmlRoot);

        // Clip the BorderPane to ensure content doesn't bleed out of rounded corners
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(loginRoot.widthProperty());
        clip.heightProperty().bind(loginRoot.heightProperty());
        clip.setArcWidth(0); // Match 2x radius
        clip.setArcHeight(0);
        loginRoot.setClip(clip);

        // --- SPLASH SCREEN (Kept your logic) ---
        StackPane splashScreen = new StackPane();
        splashScreen.setStyle("-fx-background-color: transparent; -fx-background-radius: 0;");

        try {
            ImageView splashImage = new ImageView(new Image(getClass().getResourceAsStream("/images/logoclear2.png")));
            splashImage.setFitWidth(700);
            splashImage.setFitHeight(500);
            splashImage.setPreserveRatio(true);
            splashScreen.getChildren().add(splashImage);
        } catch (Exception e) {
            Label splashLabel = new Label("St. Angelo");
            splashLabel.setTextFill(Color.web("#007345")); // Used brand color
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
        // Ensure this CSS file exists!
        scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());
        scene.setFill(Color.TRANSPARENT);

        primaryStage.setScene(scene);
        primaryStage.show();

        playStartupSequence(splashScreen, loginRoot, mainContainer);
    }

    // ... (Keep your animation methods playStartupSequence and playEntranceAnimation exactly as they were) ...

    private void playStartupSequence(StackPane splashScreen, BorderPane loginRoot, StackPane mainContainer) {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        FadeTransition fadeSplash = new FadeTransition(Duration.seconds(0.8), splashScreen);
        fadeSplash.setFromValue(1.0);
        fadeSplash.setToValue(0.0);
        fadeSplash.setOnFinished(e -> {
            mainContainer.getChildren().remove(splashScreen);
            playEntranceAnimation(loginRoot);
        });
        SequentialTransition sequence = new SequentialTransition(pause, fadeSplash);
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