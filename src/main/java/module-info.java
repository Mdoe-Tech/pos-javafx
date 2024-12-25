module com.nadia.pos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires com.zaxxer.hikari;
    requires org.postgresql.jdbc;
    requires org.apache.pdfbox;

    opens com.nadia.pos to javafx.fxml;
    exports com.nadia.pos;
    exports com.nadia.pos.model;
    exports com.nadia.pos.exceptions;
    exports com.nadia.pos.enums;
    exports com.nadia.pos.utils;

    opens com.nadia.pos.model to javafx.fxml;
    opens com.nadia.pos.controller to javafx.fxml;
}