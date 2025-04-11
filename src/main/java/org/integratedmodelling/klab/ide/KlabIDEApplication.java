package org.integratedmodelling.klab.ide;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.integratedmodelling.common.logging.Logging;
import org.eclipse.xtext.ide.server.ServerLauncher;

public class KlabIDEApplication extends Application {

  private Thread lspThread;

  @Override
  public void start(Stage stage) throws IOException {

    /*
     * TODO choose theme from settings and expose it to components
     */
    Application.setUserAgentStylesheet(Theme.CURRENT_THEME.getStylesheet());

    /*
     * TODO if local, at this point it should be enough to launch
     *  org.eclipse.xtext.ide.server.ServerLauncher to start the LSP server for all languages on the
     *  classpath.
     */
    Logging.INSTANCE.info("Starting language services for k.LAB language editors");
    this.lspThread =
        new Thread(
            () -> {
              try {
                ServerLauncher.main(new String[0]);
              } catch (Throwable t) {
                Logging.INSTANCE.error(
                    "Error launching LSP server: language services not available", t);
              }
            });

    this.lspThread.start();

    FXMLLoader fxmlLoader = new FXMLLoader(KlabIDEApplication.class.getResource("ide.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 1280, 960);
    stage.setTitle("k.LAB Modeler :: v1.0 alpha :: © 2025 Integrated Modelling Partnership");
    stage.setOnCloseRequest(
        event -> {
          /* TODO save status, ask to stop engine etc. */
        });
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
