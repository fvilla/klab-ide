package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.services.client.digitaltwin.ClientDigitalTwin;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.lang.kim.KlabDocument;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.RuntimeService;
import org.integratedmodelling.klab.api.services.runtime.Message;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.EditorPage;
import org.integratedmodelling.klab.modeler.model.NavigableProject;

import java.util.List;

public class DigitalTwinEditor extends EditorPage<RuntimeAsset> {

  private final ContextScope contextScope;
  private final RuntimeService runtimeService;
  private HBox menuArea;
  private TreeView<RuntimeAsset> treeView;
  private RuntimeAsset context;

  public DigitalTwinEditor(
      ContextScope contextScope, RuntimeService runtimeService, DigitalTwinView digitalTwinView) {
    this.contextScope = contextScope;
    this.runtimeService = runtimeService;
    if (contextScope.getDigitalTwin() instanceof ClientDigitalTwin clientDigitalTwin) {
      clientDigitalTwin.addEventConsumer(this::processEvent);
    }
    
    this.context =
        new RuntimeAsset() {

          @Override
          public long getId() {
            return 0;
          }

          @Override
          public Type classify() {
            return Type.ARTIFACT;
          }
        };
  }

  private void processEvent(Message message) {
    Logging.INSTANCE.info("Received event: " + message);
  }

  @Override
  protected void onSingleClickItemSelection(RuntimeAsset value) {}

  @Override
  protected void onDoubleClickItemSelection(RuntimeAsset value) {}

  @Override
  protected TreeView<RuntimeAsset> createContentTree() {

    treeView = new TreeView<>(defineTree(this.context));
    treeView.setCellFactory(p -> new AssetTreeCell());
    treeView.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE);
    treeView.setShowRoot(false);
    treeView.setPrefWidth(340);
    treeView.setOnContextMenuRequested(
        event -> {
          TreeItem<RuntimeAsset> item = treeView.getSelectionModel().getSelectedItem();
          if (item != null) {
            var contextMenu = new javafx.scene.control.ContextMenu();
            switch (item.getValue()) {
              //                  case NavigableProject project -> {
              //                    var lockUnlock =
              //                            new javafx.scene.control.MenuItem(project.isLocked() ?
              // "Unlock" : "Lock");
              //                    lockUnlock.setOnAction(
              //                            e -> {
              //                              if (runtimeService instanceof ResourcesService.Admin
              // admin) {
              //                                if (project.isLocked()) {
              //                                  admin.unlockProject(project.getUrn(),
              // KlabIDEController.modeler().user());
              //                                  project.setLocked(false);
              //                                } else {
              //                                  admin.lockProject(project.getUrn(),
              // KlabIDEController.modeler().user());
              //                                  project.setLocked(true);
              //                                }
              //                              }
              //                            });
              //                    contextMenu.getItems().add(lockUnlock);
              //                  }
              //                  case KlabDocument<?> document -> {
              //                    var openEdit = new javafx.scene.control.MenuItem("Open");
              //                    openEdit.setOnAction(e -> edit(item.getValue()));
              //                    contextMenu.getItems().add(openEdit);
              //                  }
              default -> {}
            }
            contextMenu.show(treeView, event.getScreenX(), event.getScreenY());
          }
        });
    return treeView;
  }

  private List<RuntimeAsset> children(RuntimeAsset asset) {
    if (asset == this.context) {
      // root observations
    } // etc
    return List.of();
  }

  private TreeItem<RuntimeAsset> defineTree(RuntimeAsset asset) {
    var root = new TreeItem<>(asset);
    for (var child : children(asset)) {
      root.getChildren().add(defineTree(child));
    }
    return root;
  }

  @Override
  protected HBox createMenuArea() {
    this.menuArea = new HBox();
    return this.menuArea;
  }

  @Override
  protected Node createEditor(RuntimeAsset asset) {
    if (asset == context) {
      return new KnowledgeGraphView(this.contextScope, this);
    }
    return null;
  }

  public RuntimeAsset getRootAsset() {
    return this.context;
  }

  private static final class AssetTreeCell extends TreeCell<RuntimeAsset> {
    @Override
    protected void updateItem(RuntimeAsset asset, boolean empty) {
      super.updateItem(asset, empty);
      if (asset != null && !empty) {
        setText(Theme.getLabel(asset));
        setGraphic(Theme.getGraphics(asset));
        switch (asset) {
          default -> {
            setStyle(null);
          }
        }

      } else {
        setText(null);
        setGraphic(null);
      }
    }
  }
}
