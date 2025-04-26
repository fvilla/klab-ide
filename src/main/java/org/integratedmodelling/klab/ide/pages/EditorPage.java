package org.integratedmodelling.klab.ide.pages;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Editor for a first-class container - resource, digital twin or workspace.
 *
 * @param <T>
 */
public abstract class EditorPage<T> extends BorderPane {

  private final BorderPane browsingArea;
  private final TabPane editorTabs;
  private final HBox menuArea;

  public EditorPage() {
    this.browsingArea = new BorderPane();
    this.menuArea = createMenuArea();
    this.editorTabs = new TabPane();
    editorTabs.setStyle(Styles.TABS_CLASSIC);
    editorTabs.setSide(Side.BOTTOM);
    browsingArea.setBottom(menuArea);
    this.setCenter(editorTabs);
    this.setRight(browsingArea);
  }

  protected void showContent() {
    Platform.runLater(
        () -> {
          browsingArea.setCenter(createContentTree());
        });
  }

  protected abstract TreeView<T> createContentTree();

  protected abstract HBox createMenuArea();
}
