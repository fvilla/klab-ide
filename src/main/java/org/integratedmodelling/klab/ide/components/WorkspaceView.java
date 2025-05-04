package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.utils.Utils;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableContainer;
import org.integratedmodelling.klab.api.view.modeler.views.ResourcesNavigator;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.ResourcesNavigatorController;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceView extends BrowsablePage<WorkspaceEditor> {

  private final ResourcesNavigatorController controller;

  private final Map<String, ResourceInfo> workspaces = new HashMap<>();
  private final Map<String, WorkspaceEditor> openEditors = new HashMap<>();
  private final Map<ResourceInfo, ResourcesService> services = new HashMap<>();
  private String localServiceId;
  private List<Node> components = new ArrayList<>();
  private Node workspaceDialog;

  public WorkspaceView() {
    this.controller =
        KlabIDEController.modeler().viewController(ResourcesNavigatorController.class);
  }

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
  protected void defineBrowser(VBox browserComponents) {

    Platform.runLater(
        () -> {
          browserComponents.getChildren().removeAll(components);
          components.clear();
          components.add(makeHeader("Workspaces", this::addWorkspace));
          if (workspaceDialog != null) {
            components.add(workspaceDialog);
          }
          for (var workspace :
              workspaces.values().stream()
                  .sorted(
                      (w1, w2) ->
                          localServiceId == null
                              ? 0
                              : (localServiceId.equals(w1.getServiceId()))
                                  ? -1
                                  : (localServiceId.equals(w2.getServiceId()) ? 1 : 0))
                  .toList()) {
            components.add(new Components.Resource(workspace, this::raiseWorkspace));
          }
          browserComponents.getChildren().addAll(components);
        });

    // TODO if we're empty and we only have one workspace, raise the workspace
  }

  private void addWorkspace() {
    workspaceDialog = createWorkspaceDialog();
    updateBrowser();
  }
  
  private Node createWorkspaceDialog() {

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
//    grid.setPadding(new Insets(2, 2, 2, 2));

    TextField workspaceTitle = new TextField();
    workspaceTitle.setPromptText("Workspace Name");
    TextArea description = new TextArea();
    description.setPromptText("Description");
    description.setPrefRowCount(3);
    var ok = new Button("OK");
    var cancel = new Button("Cancel");
    ok.setOnAction(event -> {
      // TODO launch workspace creation: when done ->
      workspaceDialog = null;
      updateBrowser();
    });
    ok.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS, Styles.SMALL);
    cancel.setOnAction(event -> {
      workspaceDialog = null;
      updateBrowser();
    });
    var buttons = new HBox(ok, cancel);
    buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
    buttons.setSpacing(4);
    cancel.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER, Styles.SMALL);
    grid.add(new Separator(), 0, 0, 2, 1);
    grid.add(new FontIcon(Theme.WORKSPACE_ICON), 0, 1);
    grid.add(workspaceTitle, 1, 1);
    grid.add(new FontIcon(Theme.EDIT_ICON), 0, 2);
    grid.add(description, 1, 2);
    grid.add(buttons, 0, 3, 2, 1);

    return grid;
    
  }

  private void raiseWorkspace(ResourceInfo resourceInfo) {

    hideBrowser();
    if (openEditors.containsKey(resourceInfo.getUrn())) {
      openEditors
          .get(resourceInfo.getUrn())
          .requestFocus(); // FIXME must remember the tabs and select(tab) - in both cases
    } else {
      var newEditor = new WorkspaceEditor(services.get(resourceInfo), resourceInfo, this);
      openEditors.put(resourceInfo.getUrn(), newEditor);
      addEditor(newEditor, resourceInfo.getUrn(), new FontIcon(Theme.WORKSPACE_ICON));
    }
  }

  public synchronized void updateServices(Engine.Status status) {
    if (status.isAvailable()) {
      workspaces.clear();
      for (var rService : KlabIDEController.modeler().user().getServices(ResourcesService.class)) {
        var capabilities = rService.capabilities(KlabIDEController.modeler().user());
        if (Utils.URLs.isLocalHost(capabilities.getUrl())) {
          this.localServiceId = capabilities.getServiceId();
        }
        for (var workspace : capabilities.getWorkspaceNames()) {
          var resourceInfo = rService.resourceInfo(workspace, KlabIDEController.modeler().user());
          workspaces.put(workspace, resourceInfo);
          services.put(resourceInfo, rService);
        }
      }
    }
    Logging.INSTANCE.info(workspaces);
  }
}
