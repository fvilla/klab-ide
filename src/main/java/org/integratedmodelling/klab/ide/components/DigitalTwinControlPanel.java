package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.RingProgressIndicator;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.integratedmodelling.common.utils.Utils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.integratedmodelling.klab.api.data.RuntimeAssetGraph;
import org.integratedmodelling.klab.api.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.integratedmodelling.klab.ide.model.DigitalTwinPeer;
import org.integratedmodelling.klab.ide.pages.EditorPage;

import javax.swing.*;

/**
 * TODO this must be paired to the DT tab in the DT editor and receive any events directed to it.
 *
 * <p>IDEA: top menu has DT choice (MenuButton) + DT switch to main view button (->) + Observer
 * label + Observer choice (MenuButton)
 *
 * <p>Center area shows context in some way and has spinner for current observation (ideally on top
 * of context). If no context show the "drop here" arrow, same size spinner if computing. Errors and
 * info message should be just small buttons getting colored; click should show the list as an
 * overlay. Full logs in full view
 *
 * <p>Bottom menu has Context choice (MenuButton) + Context label + Scenario count + Scenario choose
 * button (dialog or switch)/reset all scenarios
 */
public class DigitalTwinControlPanel extends BorderPane implements DigitalTwinViewer {

  private final RingProgressIndicator progressIndicator;
  private ContextScope scope;

  public enum Status {
    IDLE,
    COMPUTING,
    ERROR,
    RECEIVING,
    INFO
  }

  private Pane dropZone;
  private Status status = Status.IDLE;
  private TreeTableView<Activity> treeTableView;
  /* controller is bound after the first observation is made */
  private DigitalTwinPeer controller;

  // otherwise?

  public DigitalTwinControlPanel(int size, EditorPage<?> editorPage) {
    super();
    setMinHeight(size);
    setMinWidth(size);

    // Create top control bar
    HBox controlBar = new HBox(10);
    controlBar.setPrefHeight(32);
    controlBar.setAlignment(Pos.CENTER_LEFT);
    controlBar.setPadding(new Insets(5));
    controlBar.setStyle("-fx-background-color: #E0E0E0;");

    // Target selection menu
    MenuButton targetMenu = new MenuButton();
    targetMenu.setGraphic(new FontIcon(Material2AL.BARCODE));
    targetMenu.getStyleClass().addAll(Styles.FLAT, Styles.BUTTON_CIRCLE);
    // Function buttons
    Button collapseButton = new Button();
    collapseButton.setGraphic(new FontIcon(Material2AL.BARCODE));
    collapseButton.setOnAction(e -> editorPage.hideDigitalTwinControlPanel());
    collapseButton.getStyleClass().addAll(Styles.FLAT, Styles.BUTTON_CIRCLE);

    Button resetButton = new Button();
    resetButton.setGraphic(new FontIcon(Material2AL.FORMAT_COLOR_RESET));
    resetButton.setOnAction(e -> setScope(null));
    resetButton.getStyleClass().addAll(Styles.FLAT, Styles.BUTTON_CIRCLE);

    // Status label
    Label statusLabel = new Label("No target selected");
    HBox.setHgrow(statusLabel, Priority.ALWAYS);
    statusLabel.setMaxWidth(Double.MAX_VALUE);

    // Progress indicator
    this.progressIndicator = new RingProgressIndicator(0, false);
    progressIndicator.setPrefSize(24, 24);
    progressIndicator.setMaxSize(24, 24);
    progressIndicator.setMinSize(24, 24);
    progressIndicator.setProgress(1);

    controlBar
        .getChildren()
        .addAll(targetMenu, collapseButton, resetButton, statusLabel, progressIndicator);
    setTop(controlBar);

    treeTableView = new TreeTableView<>();
    treeTableView.setMinSize(220, 220);
    treeTableView.setColumnResizePolicy(TreeTableView.UNCONSTRAINED_RESIZE_POLICY);
    treeTableView.getStyleClass().addAll(Styles.DENSE, Tweaks.EDGE_TO_EDGE, Tweaks.NO_HEADER);
    treeTableView.setShowRoot(false);

    TreeTableColumn<Activity, FontIcon> typeColumn = new TreeTableColumn<>("Type");
    typeColumn.setPrefWidth(32);
    typeColumn.setCellValueFactory(
        param -> {
          FontIcon icon = new FontIcon(Material2AL.ACCOUNT_TREE);
          return new SimpleObjectProperty<>(icon);
        });

    TreeTableColumn<Activity, String> descriptionColumn = new TreeTableColumn<>("Description");
    descriptionColumn.prefWidthProperty().bind(treeTableView.widthProperty().subtract(64));

    descriptionColumn.setCellValueFactory(
        param -> new SimpleObjectProperty<>(activityDescription(param.getValue().getValue())));

    TreeTableColumn<Activity, FontIcon> statusColumn = new TreeTableColumn<>("Status");
    statusColumn.setPrefWidth(32);
    statusColumn.setCellValueFactory(
        param -> {
          FontIcon icon = new FontIcon(Material2AL.CHECK_CIRCLE);
          return new SimpleObjectProperty<>(icon);
        });

    treeTableView.getColumns().setAll(typeColumn, descriptionColumn, statusColumn);
    treeTableView.setRoot(new TreeItem<>());
    treeTableView.setShowRoot(false);

    dropZone = new Pane();
    dropZone.setMinSize(220, 220);
    dropZone.setMaxSize(220, 220);
    dropZone.setStyle(
        "-fx-background-color: #F5F5F5; -fx-border-color: grey; -fx-border-width: 5; -fx-border-style: dashed; -fx-border-radius: 10;");
    //    Label dropLabel = new Label("Drop an observable here");
    //    dropLabel.setTextFill(Color.GREY);
    //    dropZone.getChildren().add(dropLabel);
    //    dropLabel.setLayoutX((220 - dropLabel.prefWidth(-1)) / 2);
    //    dropLabel.setLayoutY((220 - dropLabel.prefHeight(-1)) / 2);
    setCenter(treeTableView);

    // Create bottom control bar for scenarios
    HBox scenarioBar = new HBox(10);
    scenarioBar.setPadding(new Insets(5));
    scenarioBar.setStyle("-fx-background-color: #E0E0E0;");

    // Scenario selection menu
    MenuButton scenarioMenu = new MenuButton();
    scenarioMenu.setGraphic(new FontIcon(Material2AL.LIBRARY_BOOKS));

    // Scenario selection combobox
    ComboBox<String> scenarioComboBox = new ComboBox<>();
    scenarioComboBox.setPromptText("Select scenario");
    HBox.setHgrow(scenarioComboBox, Priority.ALWAYS);

    // Current scenario label
    Label currentScenarioLabel = new Label("No scenario selected");
    HBox.setHgrow(currentScenarioLabel, Priority.ALWAYS);
    currentScenarioLabel.setMaxWidth(Double.MAX_VALUE);

    scenarioBar.getChildren().addAll(scenarioMenu, scenarioComboBox, currentScenarioLabel);
    setBottom(scenarioBar);
  }

  private String activityDescription(Activity value) {
    // TODO
    return Utils.Strings.abbreviate(
        Utils.Strings.replaceWhitespace(value.getDescription(), " "), 64);
  }

  public void setStatus(Status status) {
    this.status = status;
    Platform.runLater(
        () -> {
          switch (status) {
            case IDLE -> {
              progressIndicator.setProgress(0);
              setCenter(treeTableView);
            }
            case RECEIVING -> {
              setCenter(dropZone);
            }
            case COMPUTING -> {
              setCenter(treeTableView);
              progressIndicator.setProgress(-1);
            }
          }
        });
  }

  public void setScope(ContextScope scope) {

    if (this.scope != null) {
      throw new KlabInternalErrorException("SCOPE CAN ONLY BE BOUND ONCE IN A DT WIDGET");
    }
    Platform.runLater(
        () -> {
          ComboBox<String> scenarioBox =
              (ComboBox<String>) ((HBox) getBottom()).getChildren().get(1);
          scenarioBox.getItems().clear();
        });
    this.scope = scope;
    this.controller = KlabIDEController.instance().getDigitalTwinPeer(scope);
    this.controller.register(this);
    // TODO define the full interface and bind the controller
  }

  public ContextScope getScope() {
    return this.scope;
  }

  @Override
  public void submissionStarted(Observation observation) {}

  @Override
  public void submissionAborted(Observation observation) {}

  @Override
  public void submissionFinished(Observation observation) {}

  @Override
  public void setContext(Observation observation) {}

  @Override
  public void setObserver(Observation observation) {}

  @Override
  public void activityFinished(Activity activity) {
    Platform.runLater(
        () -> {
          TreeItem<Activity> existing = findTreeItemByTransientId(activity.getTransientId());
          if (existing != null) {
            existing.setValue(activity);
          }
        });
  }

  @Override
  public void activityStarted(Activity activity) {
    Platform.runLater(
        () -> {
          TreeItem<Activity> item = new TreeItem<>(activity);
          treeTableView.getRoot().getChildren().add(item);
        });
  }

  private TreeItem<Activity> findTreeItemByTransientId(long transientId) {
    if (treeTableView.getRoot() == null) return null;
    for (TreeItem<Activity> item : treeTableView.getRoot().getChildren()) {
      if (item.getValue().getTransientId() == transientId) {
        return item;
      }
    }
    return null;
  }

  @Override
  public void knowledgeGraphCommitted(RuntimeAssetGraph graph) {}

  @Override
  public void scheduleModified(Schedule schedule) {}

  @Override
  public void cleanup() {}
}
