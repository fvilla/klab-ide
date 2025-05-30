package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.services.client.digitaltwin.ClientDigitalTwin;
import org.integratedmodelling.common.services.client.digitaltwin.ClientKnowledgeGraph;
import org.integratedmodelling.klab.api.data.KnowledgeGraph;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.data.RuntimeAssetGraph;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.RuntimeService;
import org.integratedmodelling.klab.api.services.runtime.Message;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.integratedmodelling.klab.ide.pages.EditorPage;

public class DigitalTwinEditor extends EditorPage<RuntimeAsset> implements DigitalTwinViewer {

  private final ContextScope contextScope;
  private final RuntimeService runtimeService;
  private ClientKnowledgeGraph knowledgeGraph;
  private HBox menuArea;
  private TreeView<RuntimeAsset> treeView;
  private RuntimeAsset context;
  private KnowledgeGraphView knowledgeGraphView;
  private TreeItem<RuntimeAsset> root;

  public DigitalTwinEditor(
      ContextScope contextScope, RuntimeService runtimeService, DigitalTwinView digitalTwinView) {
    this.contextScope = contextScope;
    this.runtimeService = runtimeService;
    if (contextScope.getDigitalTwin() instanceof ClientDigitalTwin clientDigitalTwin) {
      clientDigitalTwin.addEventConsumer(this::processEvent);
      this.knowledgeGraph = (ClientKnowledgeGraph) clientDigitalTwin.getKnowledgeGraph();
    }

    this.context = RuntimeAsset.CONTEXT_ASSET;
    this.root = defineTree(this.context);
  }

  private void processEvent(Message message) {

    Logging.INSTANCE.info("COZZA: " + message.getMessageType());

    switch (message.getMessageType()) {
      case KnowledgeGraphCommitted -> {
        // TODO this only for the first case
        updateTree(this.context);
      }
      case ContextualizationAborted, ContextualizationSuccessful, ContextualizationStarted -> {
        // TODO insert object, define aspect
        //        if (message.getMessageType() == Message.MessageType.ContextualizationStarted) {
        //          knowledgeGraphView.setFocalAsset(message.getPayload(Observation.class));
        //        }
      }
      // FIXME sketchy logics
      case ObservationSubmissionAborted -> {}
      case ObservationSubmissionStarted -> {}
      case ObservationSubmissionFinished -> {
        submissionFinished(message.getPayload(Observation.class));
      }
    }
  }

  @Override
  protected void onSingleClickItemSelection(RuntimeAsset value) {}

  @Override
  protected void onDoubleClickItemSelection(RuntimeAsset value) {}

  @Override
  protected TreeView<RuntimeAsset> createContentTree() {

    treeView = new KnowledgeGraphTree(this.root);
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
    if (contextScope.getDigitalTwin().getKnowledgeGraph()
        instanceof ClientKnowledgeGraph clientKnowledgeGraph) {
      return clientKnowledgeGraph.outgoing(asset, GraphModel.Relationship.HAS_CHILD);
    }
    return List.of();
  }

  /**
   * Tree behavior:
   *
   * <p>on explicit observation submission received: add it in ctx with clock icon on resolved tree
   * received:
   *
   * <p>on submission failed: on submission finished: check if empty;
   *
   * @param asset
   * @return
   */
  private TreeItem<RuntimeAsset> defineTree(RuntimeAsset asset) {
    var ret = new TreeItem<>(asset);
    for (var child : children(asset)) {
      ret.getChildren().add(defineTree(child));
    }
    return ret;
  }

  @Override
  protected HBox createMenuArea() {
    this.menuArea = new HBox();
    return this.menuArea;
  }

  @Override
  protected Node createEditor(RuntimeAsset asset) {
    if (asset == context) {
      return this.knowledgeGraphView =
          new KnowledgeGraphView(this.contextScope, this.knowledgeGraph);
    }
    return null;
  }

  private TreeItem<RuntimeAsset> findTreeItemById(TreeItem<RuntimeAsset> current, long id) {
    if (current.getValue().getId() == id) {
      return current;
    }
    for (TreeItem<RuntimeAsset> child : current.getChildren()) {
      TreeItem<RuntimeAsset> result = findTreeItemById(child, id);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  private void updateTree(RuntimeAsset changed) {
    Platform.runLater(
        () -> {
          // Store selection to restore it later
          TreeItem<RuntimeAsset> selectedItem = treeView.getSelectionModel().getSelectedItem();

          // Temporarily disable cell updates to prevent flickering
          treeView.setDisable(true);

          try {
            treeView.getRoot().getChildren().clear();
            TreeItem<RuntimeAsset> newRoot = defineTree(RuntimeAsset.CONTEXT_ASSET);
            treeView.setRoot(newRoot);

            // Restore selection if possible
            if (selectedItem != null) {
              TreeItem<RuntimeAsset> newSelectedItem =
                  findTreeItemById(newRoot, selectedItem.getValue().getId());
              if (newSelectedItem != null) {
                treeView.getSelectionModel().select(newSelectedItem);
              }
            }
          } finally {
            treeView.setDisable(false);
          }
        });
  }

  public RuntimeAsset getRootAsset() {
    return this.context;
  }

  @Override
  public void submission(Observation observation) {}

  @Override
  public void submissionAborted(Observation observation) {}

  @Override
  public void submissionFinished(Observation observation) {
    this.knowledgeGraphView.setFocalAsset(observation);
  }

  @Override
  public void setContext(Observation observation) {}

  @Override
  public void setObserver(Observation observation) {}

  private static final class AssetTreeCell extends TreeCell<RuntimeAsset> {
    @Override
    protected void updateItem(RuntimeAsset asset, boolean empty) {
      super.updateItem(asset, empty);

      if (empty || asset == null) {
        // Cell is empty or asset is null - clear everything
        setText(null);
        setGraphic(null);
        setStyle(null);
      } else {
        // Cell has valid content
        setText(Theme.getLabel(asset));
        setGraphic(Theme.getGraphics(asset));
        switch (asset) {
          default -> {
            setStyle(null);
          }
        }
      }
    }
  }
}
