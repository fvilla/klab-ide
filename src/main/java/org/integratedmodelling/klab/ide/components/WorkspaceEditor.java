package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import eu.mihosoft.monacofx.MonacoFX;

import java.util.Arrays;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.utils.Utils;
import org.integratedmodelling.klab.api.knowledge.organization.ProjectStorage;
import org.integratedmodelling.klab.api.lang.kim.KlabDocument;
import org.integratedmodelling.klab.api.lang.kim.KlabStatement;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.common.data.Notification;
import org.integratedmodelling.klab.ide.KlabIDEApplication;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.EditorPage;
import org.integratedmodelling.klab.modeler.model.NavigableProject;
import org.integratedmodelling.klab.modeler.model.NavigableWorkspace;

public class WorkspaceEditor extends EditorPage<NavigableAsset> {

  private final ResourcesService service;
  private final NavigableWorkspace workspace;
  private final WorkspaceView view;

  public WorkspaceEditor(ResourcesService service, ResourceInfo resourceInfo, WorkspaceView view) {
    this.service = service;
    this.view = view;
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
  protected Node createEditor(NavigableAsset asset) {
    if (asset instanceof KlabDocument<?> document) {
      var ret = new MonacoFX();
      ret.onKeyPressedProperty()
          .setValue(
              event -> {
                if (event.isControlDown() && event.getCode() == KeyCode.S) {
                  Thread.ofVirtual().start(() -> saveDocument(ret, asset));
                }
              });
      ret.getEditor().setCurrentTheme(Theme.CURRENT_THEME.isDark() ? "vs-dark" : "vs");
      ret.getEditor().getDocument().setText(document.getSourceCode());
      // TODO language stuff
      ret.getEditor().setCurrentLanguage("java"); // right

      return ret;
    }
    return null;
  }

  private void saveDocument(MonacoFX editor, NavigableAsset asset) {
    if (service instanceof ResourcesService.Admin admin
        && asset instanceof KlabDocument<?> document) {
      var changes =
          admin.updateDocument(
              asset.parent(NavigableProject.class).getUrn(),
              ProjectStorage.ResourceType.classify(document),
              editor.getEditor().getDocument().getText(),
              KlabIDEController.modeler().user());
      updateWorkspace(changes);
    }
  }

  public void updateWorkspace(List<ResourceSet> changes) {

    Logging.INSTANCE.info("UPDATE CHANGES DIOCA " + changes);

    var allNotifications =
        Utils.Collections.flatList(changes.stream().map(ResourceSet::getNotifications).toList());
    var codeNotifications =
        allNotifications.stream()
            .filter(notification -> notification.getLexicalContext() != null)
            .toList();
    var systemNotifications =
        allNotifications.stream()
            .filter(notification -> notification.getLexicalContext() == null)
            .toList();

    if (!systemNotifications.isEmpty()
        && KlabIDEApplication.instance().handleNotifications(systemNotifications)) {
      return;
    }

    for (var change : changes) {
      if (!change.isEmpty()) {
        if (workspace.mergeChanges(change, KlabIDEController.modeler().user())) {
          // TODO store the changed docs for updating in the tree, include their parents if docs
          // have been added or removed
        }
      }
    }

    // TODO update the items corresponding to the changed documents; check out icons for document
    //  git status; use code notifications for error markers

    // TODO turn over the code notifications to any open editor for changes

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
      edit(value);
    } else if (value instanceof KlabStatement statement) {
      // TODO show editor and set the cursor there
    }
  }
}
