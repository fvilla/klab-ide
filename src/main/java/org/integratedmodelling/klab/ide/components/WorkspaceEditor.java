package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.klab.api.lang.kim.KlabDocument;
import org.integratedmodelling.klab.api.lang.kim.KlabStatement;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.ide.KlabIDEApplication;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.EditorPage;
import org.integratedmodelling.klab.modeler.model.NavigableWorkspace;

public class WorkspaceEditor extends EditorPage<NavigableAsset> {

  private final ResourcesService service;
  private final NavigableWorkspace workspace;

  public WorkspaceEditor(ResourcesService service, ResourceInfo resourceInfo) {
    this.service = service;
    this.workspace =
        new NavigableWorkspace(
            service.retrieveWorkspace(resourceInfo.getUrn(), KlabIDEController.modeler().user()));
  }

  @Override
  protected TreeView<NavigableAsset> createContentTree() {
    var treeView = new TreeView<>(defineTree(workspace));
    treeView.setCellFactory(p -> new AssetTreeCell());
    treeView.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE);
    treeView.setShowRoot(false);
    treeView.setPrefWidth(340);
    //    treeView.setPrefHeight(-1);
    //    treeView.setMaxHeight(Double.MAX_VALUE);
    //    treeView.setMaxWidth(Double.MAX_VALUE);
    return treeView;
  }

  private static final class AssetTreeCell extends TreeCell<NavigableAsset> {
    @Override
    protected void updateItem(NavigableAsset navigableAsset, boolean empty) {
      super.updateItem(navigableAsset, empty);
      if (navigableAsset != null && !empty) {
        setText(navigableAsset.getUrn());
        setGraphic(Theme.getGraphics(navigableAsset));
      } else {
        setText(null);
        setGraphic(null);
      }
    }
  }

  private TreeItem<NavigableAsset> defineTree(NavigableAsset asset) {
    var root = new TreeItem<>(asset);
    for (var child : asset.children()) {
      root.getChildren().add(defineTree(child));
    }
    return root;
  }

  @Override
  protected HBox createMenuArea() {
    var ret = new HBox();
    return ret;
  }

  @Override
  protected void onSingleClickItemSelection(NavigableAsset value) {
    if (KlabIDEApplication.instance().isInspectorShown()) {
      // TODO
    }
  }

  @Override
  protected void onDoubleClickItemSelection(NavigableAsset value) {
    if (value instanceof KlabDocument<?> document) {
      // TODO open editor in tab
      Logging.INSTANCE.info(document.getSourceCode());
    } else if (value instanceof KlabStatement statement) {
      // TODO show editor and set the cursor there
    }
  }
}
