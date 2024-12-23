module com.mdoe.nadiapos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.nadia.pos to javafx.fxml;
    exports com.nadia.pos;
    exports com.nadia.pos.model;
    opens com.nadia.pos.model to javafx.fxml;
}