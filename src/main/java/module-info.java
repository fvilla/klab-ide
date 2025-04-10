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
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.material2;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.bootstrapicons;
    requires org.kordamp.ikonli.weathericons;
    requires org.eclipse.xtext.ide;

    opens org.integratedmodelling.klab.ide to javafx.fxml;
    opens org.integratedmodelling.klab.ide.components to javafx.fxml;

    exports org.integratedmodelling.klab.ide;
    exports org.integratedmodelling.klab.ide.components;
}