module org.integratedmodelling.klab.ide {
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires klab.core.api;
    requires klab.modeler;
    requires jakarta.annotation;
    requires atlantafx.base;

    opens org.integratedmodelling.klab.ide to javafx.fxml;
    exports org.integratedmodelling.klab.ide;
}