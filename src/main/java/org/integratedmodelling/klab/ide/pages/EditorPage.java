package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.integratedmodelling.common.logging.Logging;

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

  public EditorPage() {
    this.browsingArea = new BorderPane();
    this.menuArea = createMenuArea();
    this.editorTabs = new TabPane();
    editorTabs.setStyle(Styles.TABS_CLASSIC);
    editorTabs.setSide(Side.BOTTOM);
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
                TreeItem<?> item = tree.getSelectionModel().getSelectedItem();
                if (isClickTimelinePlaying) {
                  // when clicking the second time before the time line finishes
                  isClickTimelinePlaying = false;
                  Thread.ofVirtual().start(() -> onDoubleClickItemSelection((T) item.getValue()));
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
                          Thread.ofVirtual()
                              .start(() -> onSingleClickItemSelection((T) item.getValue()));
                        }
                        isClickTimelinePlaying = false;
                      });
                  clickTimeline.play();
                }
              });

          browsingArea.setCenter(tree);
        });
  }

  protected abstract void onSingleClickItemSelection(T value);

  protected abstract void onDoubleClickItemSelection(T value);

  protected abstract TreeView<T> createContentTree();

  protected abstract HBox createMenuArea();
}
