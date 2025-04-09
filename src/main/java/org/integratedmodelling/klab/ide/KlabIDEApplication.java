package org.integratedmodelling.klab.ide;

import java.io.IOException;
import java.util.Base64;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.services.runtime.Notification;
import org.integratedmodelling.klab.api.view.UI;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.modeler.ModelerImpl;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KlabIDEApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {

    /*
     * TODO choose theme from settings and expose it to components
     */
    Application.setUserAgentStylesheet(Theme.CURRENT_THEME.getStylesheet());

    FXMLLoader fxmlLoader = new FXMLLoader(KlabIDEApplication.class.getResource("ide.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 1280, 960);
    stage.setTitle("k.LAB Modeler :: v1.0 alpha :: © 2025 Integrated Modelling Partnership");
    stage.setOnCloseRequest(
        event -> {
          /* TODO save status, ask to stop engine etc. */
        });
//    String cssString =
//        String.format(
//            ".ikonli-font-icon {-fx-icon-size: %d;}",
//            24); // TODO use a size based on screen resolution
//    scene.getRoot().getStylesheets().removeIf(uri -> uri.startsWith("data:text/css"));
//    scene
//        .getRoot()
//        .getStylesheets()
//        .add(
//            "data:text/css;base64,"
//                + Base64.getEncoder().encodeToString(cssString.toString().getBytes(UTF_8)));
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
