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

    opens com.mdoe.nadiapos to javafx.fxml;
    exports com.mdoe.nadiapos;
    exports com.mdoe.nadiapos.model;
    opens com.mdoe.nadiapos.model to javafx.fxml;
}