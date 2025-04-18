package org.integratedmodelling.klab.ide.components;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
import org.integratedmodelling.klab.ide.pages.Page;

import java.io.IOException;

public class NotebookView extends BorderPane implements Page {

  @FXML private TextField inputBox;
  @FXML private VBox notebook;

  public NotebookView() {
    var fxmlLoader = new FXMLLoader(getClass().getResource("notebook-view.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    addComponent(new Components.About());
  }

  @FXML
  protected void initialize() {
    inputBox.setText("Parent is " + getView());
  }

  @Override
  public String getName() {
    return "Dashboard";
  }

  @Override
  public Parent getView() {
    return this; // dio porco
  }

  public void addComponent(Components.Component component) {
    if (component instanceof Node node) {
      Platform.runLater(
          () -> {
            // TODO add to index list
            notebook.getChildren().add(node);
          });
    }
  }

  @Override
  public void reset() {

  }
}
