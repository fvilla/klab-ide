package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.components.DigitalTwinWidget;
import org.integratedmodelling.klab.ide.utils.NodeUtils;

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
  private final Node menuArea;
  final Timeline clickTimeline = new Timeline();
  Duration clickDuration = Duration.millis(350);
  KeyFrame clickKeyFrame = new KeyFrame(clickDuration);
  boolean isClickTimelinePlaying = false;
  private Map<T, Tab> assetEditors = new HashMap<>();
  protected DigitalTwinWidget digitalTwinMinified;
  private TreeView<T> tree;

  public EditorPage() {
    this.browsingArea = new BorderPane();
    this.menuArea = createMenuArea();
    menuArea.setStyle("-fx-background-color: -color-neutral-subtle;");
    if (menuArea instanceof Region region) {
      region.setPrefHeight(44);
    }
    this.editorTabs = new TabPane();
    this.editorTabs.getStyleClass().add(Styles.TABS_CLASSIC);
    this.editorTabs.setSide(Side.BOTTOM);
    browsingArea.setBottom(menuArea);

    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getItems().addAll(editorTabs, browsingArea);
    splitPane.setDividerPositions(0.7);
    this.setCenter(splitPane);

    clickTimeline.getKeyFrames().add(clickKeyFrame);
  }

  protected void configureDigitalTwinWidget(DigitalTwinWidget digitalTwinMinified) {}

  protected void showContent() {
    Platform.runLater(
        () -> {
          this.tree = createContentTree();
          this.tree.setOnMouseClicked(
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

          var container = new VBox();
          VBox.setVgrow(tree, Priority.ALWAYS);
          container.setMaxWidth(Double.MAX_VALUE);
          tree.setMaxWidth(Double.MAX_VALUE);
          digitalTwinMinified = new DigitalTwinWidget(tree.widthProperty().intValue(), this);
          digitalTwinMinified.prefWidthProperty().bind(tree.widthProperty());
          digitalTwinMinified.prefHeightProperty().bind(digitalTwinMinified.widthProperty());
          configureDigitalTwinWidget(digitalTwinMinified);

          NodeUtils.toggleVisibility(digitalTwinMinified, false);

          container.getChildren().addAll(tree, digitalTwinMinified);

          browsingArea.setCenter(container);
        });
  }

  protected void showDigitalTwinMinified() {
    if (!digitalTwinMinified.isVisible()) {
      Platform.runLater(
          () -> {
            NodeUtils.toggleVisibility(digitalTwinMinified, true);
          });
    }
  }

  protected void hideDigitalTwinMinified() {
    if (digitalTwinMinified.isVisible()) {
      Platform.runLater(
          () -> {
            NodeUtils.toggleVisibility(digitalTwinMinified, false);
          });
    }
  }

  public void edit(T asset) {
    if (!assetEditors.containsKey(asset)) {
      var editor = createEditor(asset);
      if (editor != null) {
        var tab = new Tab(Theme.getLabel(asset), editor);
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

  protected abstract Node createMenuArea();
}
