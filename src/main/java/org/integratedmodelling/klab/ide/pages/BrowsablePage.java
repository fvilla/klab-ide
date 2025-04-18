package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.controls.ModalPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;

/** The generic browser with a modal index on the left. */
public abstract class BrowsablePage<T extends Node> extends StackPane implements Page {

  private static class Dialog extends VBox {

    public Dialog(int width, int height) {
      super();

      setSpacing(10);
      setAlignment(Pos.CENTER);
      setMinSize(width, height);
      setMaxSize(width, height);
      setStyle("-fx-background-color: -color-bg-default;");
    }
  }

  private final ModalPane modalPane = new ModalPane();
  private Dialog browserArea;

  protected BrowsablePage() {
    getChildren().add(modalPane);
    this.browserArea = new Dialog(280, -1);
    modalPane.setOnMouseMoved(
        event -> {
          if (event.getSceneX() < 100 && event.getSceneY() < 100) {
            showBrowser();
          }
        });
  }

  protected abstract void defineBrowser(VBox vBox);

  public void showBrowser() {

    if (modalPane.contentProperty().isBound()) {
      return;
    }

    Platform.runLater(
        () -> {
          this.browserArea.getChildren().removeAll();
          defineBrowser(this.browserArea);
          modalPane.setAlignment(Pos.TOP_LEFT);
          modalPane.usePredefinedTransitionFactories(Side.LEFT);
          modalPane.show(browserArea);
        });
  }
}
