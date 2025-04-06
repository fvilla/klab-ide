package org.integratedmodelling.klab.ide.components;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class NotebookView extends BorderPane {

  @FXML private TextField inputBox;

  public NotebookView() {
    var fxmlLoader = new FXMLLoader(getClass().getResource("notebook-view.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
