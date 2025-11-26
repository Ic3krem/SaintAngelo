package com.stangelo.saintangelo.app;

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
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApp extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.initStyle(StageStyle.UNDECORATED);

        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("/fxml/login-view.fxml"));

        // --- TITLE BAR SETUP ---

        // 1. Icon (Optional: Replace with your actual logo path)
        // ImageView appIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/logo_small.png")));
        // appIcon.setFitHeight(20);
        // appIcon.setFitWidth(20);

        // 2. Title Label
        Label titleLabel = new Label("Saint Angelo Medical Center");
        titleLabel.getStyleClass().add("title-label");

        // 3. Container for Title (Icon + Text)
        HBox titleContainer = new HBox(10); // 10px spacing
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        // titleContainer.getChildren().addAll(appIcon, titleLabel); // Uncomment if using icon
        titleContainer.getChildren().add(titleLabel);

        // 4. Spacer
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Icons using SVG Paths (Cleaner than text)
        SVGPath minIcon = new SVGPath();
        minIcon.setContent("M0 8h10v2H0z"); // Simple line
        minIcon.getStyleClass().add("window-icon");

        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M10.7 9.3l-3.3-3.3 3.3-3.3c.4-.4.4-1 0-1.4s-1-.4-1.4 0l-3.3 3.3-3.3-3.3c-.4-.4-1-.4-1.4 0s-.4 1 0 1.4l3.3 3.3-3.3 3.3c-.4.4-.4 1 0 1.4.2.2.5.3.7.3s.5-.1.7-.3l3.3-3.3 3.3 3.3c.2.2.5.3.7.3s.5-.1.7-.3c.4-.4.4-1 0-1.4z");
        closeIcon.getStyleClass().add("window-icon");

        // 6. Buttons
        Button minimizeButton = new Button();
        minimizeButton.setGraphic(minIcon);
        minimizeButton.getStyleClass().add("title-bar-button");
        minimizeButton.setOnAction(event -> primaryStage.setIconified(true));

        Button closeButton = new Button();
        closeButton.setGraphic(closeIcon);
        closeButton.getStyleClass().addAll("title-bar-button", "close-button");
        closeButton.setOnAction(event -> primaryStage.close());

        HBox controlButtons = new HBox(0); // No spacing, buttons touch like Windows/Browser
        controlButtons.setAlignment(Pos.CENTER_RIGHT);
        controlButtons.getChildren().addAll(minimizeButton, closeButton);

        // 7. Main Title Bar Layout
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.getStyleClass().add("title-bar");
        titleBar.getChildren().addAll(titleContainer, spacer, controlButtons);

        // --- DRAG LOGIC ---
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        // --- ROOT SETUP ---
        BorderPane root = new BorderPane();
        root.setTop(titleBar);
        root.setCenter(fxmlRoot);

        // Apply drop shadow effect to the whole window (Optional, makes it pop)
        root.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0); -fx-background-color: white;");

        Scene scene = new Scene(root);
        // Ensure this path matches your project structure
        scene.getStylesheets().add(getClass().getResource("/css/main-app.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}