package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.integratedmodelling.klab.ide.Theme;

import java.util.HashMap;
import java.util.Map;

/**
 * Editor for a first-class container - resource, digital twin or workspace.
 *
 * @param <T>
 */
public abstract class EditorPage<T> extends BorderPane {

  private final BorderPane browsingArea;
  private final TabPane editorTabs;
  private final HBox menuArea;
  final Timeline clickTimeline = new Timeline();
  Duration clickDuration = Duration.millis(350);
  KeyFrame clickKeyFrame = new KeyFrame(clickDuration);
  boolean isClickTimelinePlaying = false;
  private Map<T, Tab> assetEditors = new HashMap<>();

  public EditorPage() {
    this.browsingArea = new BorderPane();
    this.menuArea = createMenuArea();
    this.editorTabs = new TabPane();
    browsingArea.setBottom(menuArea);
    this.setCenter(editorTabs);
    this.setRight(browsingArea);
    clickTimeline.getKeyFrames().add(clickKeyFrame);
  }

  protected void showContent() {
    Platform.runLater(
        () -> {
          var tree = createContentTree();
          tree.setOnMouseClicked(
              event -> {
                // painful
                TreeItem<?> item = tree.getSelectionModel().getSelectedItem();
                if (isClickTimelinePlaying) {
                  // when clicking the second time before the time line finishes
                  isClickTimelinePlaying = false;
                  onDoubleClickItemSelection((T) item.getValue());
                  clickTimeline.stop();
                } else {
                  // when clicking for the first time
                  isClickTimelinePlaying = true;
                  // start the timeline
                  // if timeline finises without receiving a second click, consider it a single
                  // click
                  clickTimeline.setOnFinished(
                      x -> {
                        if (item != null) {
                          onSingleClickItemSelection((T) item.getValue());
                        }
                        isClickTimelinePlaying = false;
                      });
                  clickTimeline.play();
                }
              });

          browsingArea.setCenter(tree);
        });
  }

  protected void edit(T asset) {
    if (!assetEditors.containsKey(asset)) {
      var editor = createEditor(asset);
      if (editor != null) {
        var tab = new Tab(Theme.getLabel(asset), /* TODO actual name */ editor);
        tab.setGraphic(Theme.getGraphics(asset));
        editorTabs.getTabs().add(tab);
        assetEditors.put(asset, tab);
        editorTabs.getSelectionModel().select(tab);
      }
    }
    if (assetEditors.containsKey(asset)) {
      editorTabs.getSelectionModel().select(assetEditors.get(asset));
    }
  }

  protected abstract Node createEditor(T asset);

  /**
   * Handle a single click in the browse tree. Note: runs inside the platform UI thread
   *
   * @param value
   */
  protected abstract void onSingleClickItemSelection(T value);

  /**
   * Handle a double click in the browse tree. Note: runs inside the platform UI thread
   *
   * @param value
   */
  protected abstract void onDoubleClickItemSelection(T value);

  protected abstract TreeView<T> createContentTree();

  protected abstract HBox createMenuArea();
}
