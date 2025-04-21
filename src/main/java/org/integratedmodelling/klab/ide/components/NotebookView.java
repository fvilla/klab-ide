package org.integratedmodelling.klab.ide.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.integratedmodelling.klab.ide.pages.OutlinePage;
import org.integratedmodelling.klab.ide.pages.Page;

public class NotebookView extends BorderPane implements Page {

  private final InputBox inputBox;
  private final Notebook notebook;

  public NotebookView() {

    this.notebook = new Notebook();
    this.setCenter(this.notebook);
    this.inputBox = new InputBox();
    this.setBottom(inputBox);
    this.setCenter(this.notebook);

    addComponent(new Components.About());
  }

  public static class InputBox extends TextField {
    InputBox() {
      super();
      setMargin(this, new Insets(24, 20, 20, 10));
      setPromptText("Enter a command; 'help' for assistance");
    }
  }

  public static class Notebook extends OutlinePage {

    @Override
    public String getName() {
      return "Notebook";
    }
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
            notebook.addSection(component.getTitle(), node);
          });
    }
  }

  @Override
  public void reset() {
    notebook.reset();
  }
}
