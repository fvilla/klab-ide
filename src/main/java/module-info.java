module org.integratedmodelling.klab.ide {
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires klab.core.api;
    requires klab.modeler;
    requires jakarta.annotation;
    requires atlantafx.base;
    requires klab.core.common;

    opens org.integratedmodelling.klab.ide to javafx.fxml;
    opens org.integratedmodelling.klab.ide.components to javafx.fxml;

    exports org.integratedmodelling.klab.ide;
    exports org.integratedmodelling.klab.ide.components;
}