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
    requires java.desktop;
    requires com.ibm.icu;
    requires org.eclipse.emf.ecore;
    requires jdk.jsobject;
    requires org.kordamp.ikonli.evaicons;
    requires com.google.common;
    requires org.jline;
    requires eu.mihosoft.monacofx;

    opens org.integratedmodelling.klab.ide to javafx.fxml;
    opens org.integratedmodelling.klab.ide.components to javafx.fxml;

    exports org.integratedmodelling.klab.ide;
    exports org.integratedmodelling.klab.ide.components;
    exports org.integratedmodelling.klab.ide.pages;
    exports org.integratedmodelling.klab.ide.api;
    opens org.integratedmodelling.klab.ide.api to javafx.fxml;
}