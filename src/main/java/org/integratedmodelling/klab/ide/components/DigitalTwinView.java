package org.integratedmodelling.klab.ide.components;

import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.RuntimeService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.Map;

public class DigitalTwinView extends BrowsablePage<DigitalTwinEditor> {

  private final Map<String, DigitalTwinEditor> openEditors = new HashMap<>();

  @Override
  public String getName() {
    return "Digital Twins";
  }

  @Override
  public Parent getView() {
    return this;
  }

  @Override
  public void reset() {}

  @Override
  protected void defineBrowser(VBox vBox) {}

  public void raiseDigitalTwin(ContextScope scope, RuntimeService service) {

    hideBrowser();
    if (openEditors.containsKey(scope.getId())) {
      openEditors
              .get(scope.getId())
              .requestFocus(); // FIXME must remember the tabs and select(tab) - in both cases
    } else {
      var newEditor = new DigitalTwinEditor(scope, service, this);
      openEditors.put(scope.getId(), newEditor);
      addEditor(newEditor, scope.getName(), new FontIcon(Theme.DIGITAL_TWINS_ICON));
      newEditor.edit(newEditor.getRootAsset());
    }
  }
}
