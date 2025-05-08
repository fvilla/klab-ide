package org.integratedmodelling.klab.ide.components;

import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;

public class InspectorView extends HBox {
  public InspectorView() {
    super();
    setMinHeight(300);
  }

  public void inspect(Object value) {}
}
