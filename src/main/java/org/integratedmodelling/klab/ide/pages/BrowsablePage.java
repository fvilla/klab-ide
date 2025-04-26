package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.controls.ModalPane;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;

/** The generic browser with a modal index on the left. */
public abstract class BrowsablePage<T extends Node> extends StackPane implements Page {

  private final TabPane tabPane;

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
    super();
    this.browserArea = new Dialog(380, -1);
    this.browserArea.setAlignment(Pos.TOP_CENTER);
    this.browserArea.setPadding(new Insets(2.0));
    this.tabPane = new TabPane();
    tabPane.setOnMouseMoved(
        event -> {
          if (event.getSceneX() < 100 && event.getSceneY() < 100) {
            showBrowser();
          }
        });
    getChildren().addAll(tabPane, modalPane);
  }

  public void addEditor(EditorPage<?> node, String title /* TODO icon */) {
    var tab = new Tab(title, node);
    Platform.runLater(
        () -> {
          this.tabPane.getTabs().add(tab);
          node.showContent();
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
