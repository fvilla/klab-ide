package org.integratedmodelling.klab.ide.components;

import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;

public class WorkspaceView extends BrowsablePage {

  @Override
  public String getName() {
    return "Workspaces";
  }

  @Override
  public Parent getView() {
    return this;
  }

  @Override
  public void reset() {}

  @Override
  protected void defineBrowser(VBox vBox) {}
}
