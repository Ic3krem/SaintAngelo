module com.stangelo.saintangelo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.logging;
    requires java.sql;

    // Export the main app package
    exports com.stangelo.saintangelo.app;

    // Export your model, service, and controller packages
    exports com.stangelo.saintangelo.models;
    exports com.stangelo.saintangelo.services;
    exports com.stangelo.saintangelo.controllers;

    // Open the controllers package to JavaFX FXML for reflection
    opens com.stangelo.saintangelo.controllers to javafx.fxml;
}
