package org.integratedmodelling.klab.ide.components;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.api.utils.Utils;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableContainer;
import org.integratedmodelling.klab.api.view.modeler.views.ResourcesNavigator;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.ResourcesNavigatorController;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;

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
  protected void defineBrowser(VBox vBox) {
    Platform.runLater(
        () -> {
          vBox.getChildren().removeAll(components);
          components.clear();
          for (var workspace :
              workspaces.values().stream()
                  .sorted(
                      (w1, w2) ->
                          localServiceId == null
                              ? 0
                              : (localServiceId.equals(w1.getServiceId()))
                                  ? 0
                                  : (localServiceId.equals(w2.getServiceId()) ? 0 : -1))
                  .toList()) {
            components.add(new Components.Resource(workspace, this::raiseWorkspace));
          }
          vBox.getChildren().addAll(components);
        });
  }

  private void raiseWorkspace(ResourceInfo resourceInfo) {
    if (openEditors.containsKey(resourceInfo.getUrn())) {
      openEditors.get(resourceInfo.getUrn()).requestFocus();
    } else {
      var newEditor = new WorkspaceEditor(services.get(resourceInfo), resourceInfo);
      openEditors.put(resourceInfo.getUrn(), newEditor);
      addEditor(newEditor, resourceInfo.getUrn());
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
