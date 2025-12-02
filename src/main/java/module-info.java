module com.stangelo.saintangelo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // Database and logging modules
    requires java.sql;
    requires java.logging;

    // Export the main app package
    exports com.stangelo.saintangelo.app;

    // Export your model, service, and controller packages
    exports com.stangelo.saintangelo.models;
    exports com.stangelo.saintangelo.services;
    exports com.stangelo.saintangelo.controllers;
    exports com.stangelo.saintangelo.utils;
    exports com.stangelo.saintangelo.dao;

    // Open services package for reflection (if needed)
    opens com.stangelo.saintangelo.services;

    // Open the controllers package to JavaFX FXML for reflection
    opens com.stangelo.saintangelo.controllers to javafx.fxml;
}
