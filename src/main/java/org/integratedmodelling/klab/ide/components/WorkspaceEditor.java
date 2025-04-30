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
import org.integratedmodelling.common.utils.Utils;
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
  protected HBox createMenuArea() {
    var ret = new HBox();
    return ret;
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
      updateWorkspace(changes);
    }
  }

  public void updateWorkspace(List<ResourceSet> changes) {

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

    Map<String, NavigableAsset> changed = new LinkedHashMap<>();
    for (var change : changes) {
      if (!change.isEmpty()) {
        for (var asset : workspace.mergeChanges(change, KlabIDEController.modeler().user())) {
          changed.put(asset.toString(), asset);
        }
      }
    }

    Platform.runLater(
        () -> {
          updateTree(this.root, changed);
        });
  }

  private void updateTree(TreeItem<NavigableAsset> root, Map<String, NavigableAsset> changed) {
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
