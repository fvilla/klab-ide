package org.integratedmodelling.klab.ide.components;

import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import java.util.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Separator;
import org.integratedmodelling.klab.api.knowledge.organization.ProjectStorage;
import org.integratedmodelling.klab.api.lang.kim.KlabDocument;
import org.integratedmodelling.klab.api.lang.kim.KlabStatement;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableDocument;
import org.integratedmodelling.klab.ide.KlabIDEApplication;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.contrib.monaco.MonacoEditor;
import org.integratedmodelling.klab.ide.pages.EditorPage;
import org.integratedmodelling.klab.modeler.model.NavigableKimConceptStatement;
import org.integratedmodelling.klab.modeler.model.NavigableKimModel;
import org.integratedmodelling.klab.modeler.model.NavigableProject;
import org.integratedmodelling.klab.modeler.model.NavigableWorkspace;

public class WorkspaceEditor extends EditorPage<NavigableAsset> {

  private final ResourcesService service;
  private final NavigableWorkspace workspace;
  private final WorkspaceView view;
  private TreeItem<NavigableAsset> root;

  public WorkspaceEditor(ResourcesService service, ResourceInfo resourceInfo, WorkspaceView view) {
    this.service = service;
    this.view = view;
    this.workspace =
        new NavigableWorkspace(
            service.retrieveWorkspace(resourceInfo.getUrn(), KlabIDEController.modeler().user()));
    if (service.isExclusive() && service instanceof ResourcesService.Admin admin) {
      // lock all projects that let us
      for (var project : workspace.getProjects()) {
        if (admin.lockProject(project.getUrn(), KlabIDEController.modeler().user())
            && project instanceof NavigableProject navigableProject) {
          navigableProject.setLocked(true);
        }
      }
    }
  }

  @Override
  protected TreeView<NavigableAsset> createContentTree() {
    var treeView = new TreeView<>(this.root = defineTree(workspace));
    treeView.setCellFactory(p -> new AssetTreeCell());
    treeView.getStyleClass().addAll(Tweaks.EDGE_TO_EDGE, Styles.DENSE);
    treeView.setShowRoot(false);
    treeView.setPrefWidth(340);
    treeView.setOnContextMenuRequested(
        event -> {
          TreeItem<NavigableAsset> item = treeView.getSelectionModel().getSelectedItem();
          if (item != null) {
            var contextMenu = new javafx.scene.control.ContextMenu();
            switch (item.getValue()) {
              case NavigableProject project -> {
                var lockUnlock =
                    new javafx.scene.control.MenuItem(project.isLocked() ? "Unlock" : "Lock");
                lockUnlock.setOnAction(
                    e -> {
                      if (service instanceof ResourcesService.Admin admin) {
                        if (project.isLocked()) {
                          admin.unlockProject(project.getUrn(), KlabIDEController.modeler().user());
                          project.setLocked(false);
                        } else {
                          admin.lockProject(project.getUrn(), KlabIDEController.modeler().user());
                          project.setLocked(true);
                        }
                      }
                    });
                contextMenu.getItems().add(lockUnlock);
              }
              case KlabDocument<?> document -> {
                var openEdit = new javafx.scene.control.MenuItem("Open");
                openEdit.setOnAction(e -> edit(item.getValue()));
                contextMenu.getItems().add(openEdit);
              }
              default -> {}
            }
            contextMenu.show(treeView, event.getScreenX(), event.getScreenY());
          }
        });

    return treeView;
  }

  private static final class AssetTreeCell extends TreeCell<NavigableAsset> {
    @Override
    protected void updateItem(NavigableAsset asset, boolean empty) {
      super.updateItem(asset, empty);
      if (asset != null && !empty) {
        setText(Theme.getLabel(asset));
        setGraphic(Theme.getGraphics(asset));
        switch (asset) {
          case NavigableProject navigableProject -> {
            if (navigableProject.isLocked()) {
              setStyle("-fx-text-fill: -color-success-fg;");
            }
          }
          case NavigableDocument navigableProject -> {
            // leave these - there is an unclear style "leaking" phenomenon otherwise
            setStyle(null);
          }
          case NavigableKimConceptStatement navigableProject -> {
            setStyle(null);
          }
          case NavigableKimModel navigableProject -> {
            setStyle(null);
          }
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

  private TreeItem<NavigableAsset> defineTree(NavigableAsset asset) {
    var root = new TreeItem<>(asset);
    for (var child : asset.children()) {
      root.getChildren().add(defineTree(child));
    }
    return root;
  }

  @Override
  protected Region createMenuArea() {
    var ret = new HBox();
    var separator = new Separator();
    separator.setPrefHeight(3);
    separator.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));
    return new VBox(separator, ret);
  }

  @Override
  protected Node createEditor(NavigableAsset asset) {
    if (asset instanceof KlabDocument<?> document) {
      return new MonacoEditor(
          editor -> {
            editor
                .onKeyPressedProperty()
                .setValue(
                    event -> {
                      if (event.isControlDown() && event.getCode() == KeyCode.S) {
                        Thread.ofVirtual().start(() -> saveDocument(editor, asset));
                      }
                    });
            editor.getEditor().setCurrentTheme(Theme.CURRENT_THEME.isDark() ? "vs-dark" : "vs");
            var project = asset.parent(NavigableProject.class);
            if (project != null && !project.isLocked()) {
              editor.getEditor().getViewController().readOnly(true);
            }
            editor.getEditor().getDocument().setText(document.getSourceCode());
            // TODO language stuff
            editor.getEditor().setCurrentLanguage("java"); // right
          });
    }
    return null;
  }

  private void saveDocument(MonacoEditor editor, NavigableAsset asset) {
    if (service instanceof ResourcesService.Admin admin
        && asset instanceof KlabDocument<?> document) {
      var changes =
          admin.updateDocument(
              asset.parent(NavigableProject.class).getUrn(),
              ProjectStorage.ResourceType.classify(document),
              editor.getEditor().getDocument().getText(),
              KlabIDEController.modeler().user());
      var workspaceChanges =
          changes.stream()
              .filter(ch -> this.workspace.getUrn().equals(ch.getWorkspace()))
              .findFirst();
      workspaceChanges.ifPresent(this::updateWorkspace);
    }
  }

  public void updateWorkspace(ResourceSet changes) {

    var codeNotifications =
        changes.getNotifications().stream()
            .filter(notification -> notification.getLexicalContext() != null)
            .toList();
    var systemNotifications =
        changes.getNotifications().stream()
            .filter(notification -> notification.getLexicalContext() == null)
            .toList();

    if (!systemNotifications.isEmpty()
        && KlabIDEApplication.instance().handleNotifications(systemNotifications)) {
      return;
    }

    /*
    TODO code notifications must be shown in the editor for their asset
     */

    Map<String, NavigableAsset> changed = new LinkedHashMap<>();
    if (!changes.isEmpty()) {
      for (var asset : workspace.mergeChanges(changes, KlabIDEController.modeler().user())) {
        changed.put(asset.toString(), asset);
      }
    }

    Platform.runLater(
        () -> {
          updateTree(this.root, changed);
        });
  }

  private void updateTree(TreeItem<NavigableAsset> root, Map<String, NavigableAsset> changed) {

    /*
    1. Find the tree nodes corresponding to the asset that has changed. All of those should be documents for
    the time being, but it's not impossible that this changes in the future.

    If node found: asset may be REMOVED or UPDATED. If removed, remove node; else call updateNode()
    If node not found: asset was ADDED. Find the place for it - it can be a project or a document, so find insertion point based on type and insert in alphabetical order, then
    select it. If a doc, bring it in a new editor.
     */

    /*
    2. Each root changed should be matched with their potentially new structure. Nodes may have been removed, added or changed. If we find the
        node corresponding to a child of the asset, we substitute the value in it and let the tree model do the rest. Otherwise we remove what
        is not in the asset children and add what is not in the node structure. We can simply look sequentially because the sequence should be
        the same.
     */

    if (root.getValue() != null) {
      if (changed.containsKey(root.getValue().toString())) {
        var newAsset = changed.get(root.getValue().toString());
        boolean open = root.isExpanded();
        ObservableList<TreeItem<NavigableAsset>> children = FXCollections.observableArrayList();
        for (var child : newAsset.children()) {
          children.add(defineTree(child));
        }
        root.getChildren().setAll(children);
        if (open) {
          root.setExpanded(true);
        }
      }
    }
    for (var child : root.getChildren()) {
      updateTree(child, changed);
    }
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
