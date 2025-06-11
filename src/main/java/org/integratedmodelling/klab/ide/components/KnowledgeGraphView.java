package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.services.client.digitaltwin.ClientKnowledgeGraph;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.jgrapht.graph.DefaultEdge;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

public class KnowledgeGraphView extends BorderPane implements DigitalTwinViewer {

  private final ClientKnowledgeGraph knowledgeGraph;
  private final ContextScope scope;
  private final DigitalTwinEditor editor;
  private boolean autoLayout = true;
  private SmartGraphPanel<RuntimeAsset, ClientKnowledgeGraph.Relationship> graphView;
  private int depth = 3;
  private Set<GraphModel.Relationship> relationships =
      EnumSet.of(GraphModel.Relationship.HAS_CHILD);
  private RuntimeAsset focalAsset = null;
  private boolean initialized;
  private Timeline timeline;

  public KnowledgeGraphView(
      ContextScope scope, ClientKnowledgeGraph knowledgeGraph, DigitalTwinEditor editor) {

    this.scope = scope;
    this.knowledgeGraph = knowledgeGraph;
    this.editor = editor;

    HBox controls = new HBox(2);
    controls.getStyleClass().add(Styles.SMALL);
    controls.setStyle("-fx-padding: 5px;");

    HBox switchesBox = new HBox(2);
    switchesBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
    switchesBox.getStyleClass().add(Styles.SMALL);

    ToggleSwitch switch1 = new ToggleSwitch("Children");
    ToggleSwitch switch2 = new ToggleSwitch("Affected");
    ToggleSwitch switch3 = new ToggleSwitch("Data");
    ToggleSwitch switch4 = new ToggleSwitch("Activities");
    ToggleSwitch switch5 = new ToggleSwitch("Actuators");

    switch1.setSelected(true);
    switch1.getStyleClass().addAll(Styles.SMALL, Styles.TEXT_SMALL);
    switch2.getStyleClass().addAll(Styles.SMALL, Styles.TEXT_SMALL);
    switch3.getStyleClass().addAll(Styles.SMALL, Styles.TEXT_SMALL);
    switch4.getStyleClass().addAll(Styles.SMALL, Styles.TEXT_SMALL);
    switch5.getStyleClass().addAll(Styles.SMALL, Styles.TEXT_SMALL);

    Button homeButton = new Button();
    homeButton.setGraphic(new FontIcon(Material2AL.HOME));
    homeButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.SMALL);
    Button minusButton = new Button();
    minusButton.setGraphic(new FontIcon(Material2MZ.REMOVE_CIRCLE_OUTLINE));
    minusButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.SMALL);
    Button plusButton = new Button();
    plusButton.setGraphic(new FontIcon(Material2AL.ADD_CIRCLE_OUTLINE));
    plusButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.SMALL);
    Button redrawButton = new Button();
    redrawButton.setGraphic(new FontIcon(Material2AL.AUTORENEW));
    redrawButton.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT, Styles.SMALL);

    homeButton.setOnAction(
        event -> {
          focalAsset = RuntimeAsset.CONTEXT_ASSET;
          if (initialized) {
            updateGraph(graphView.getModel(), focalAsset);
          }
        });
    minusButton.setOnAction(
        event -> {
          if (depth > 1) {
            depth--;
            if (initialized && focalAsset != null) {
              updateGraph(graphView.getModel(), focalAsset);
            }
          }
        });
    plusButton.setOnAction(
        event -> {
          if (depth < 5) {
            depth++;
            if (initialized && focalAsset != null) {
              updateGraph(graphView.getModel(), focalAsset);
            }
          }
        });
    redrawButton.setOnAction(
        event -> {
          if (depth < 5) {
            depth++;
            if (initialized && focalAsset != null) {
              autoLayout = !autoLayout;
              graphView.setAutomaticLayout(autoLayout);
            }
          }
        });
    switch1.selectedProperty().addListener((obs, old, val) -> {});
    switch2.selectedProperty().addListener((obs, old, val) -> {});
    switch3.selectedProperty().addListener((obs, old, val) -> {});
    switch4.selectedProperty().addListener((obs, old, val) -> {});
    switch5.selectedProperty().addListener((obs, old, val) -> {});

    switchesBox.getChildren().addAll(switch1, switch2, switch3, switch4, switch5);
    HBox spinnerBox = new HBox(homeButton, minusButton, plusButton, redrawButton);
    HBox.setHgrow(spinnerBox, javafx.scene.layout.Priority.ALWAYS);
    controls.getChildren().addAll(spinnerBox, switchesBox);
    this.setTop(controls);

    this.sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (!initialized && newScene != null) {
                // Delay initialization until the next pulse to ensure proper layout
                Platform.runLater(() -> initializeGraphView());
              }
            });

    KlabIDEController.instance().getDigitalTwinPeer(scope).register(this);
  }

  private void initializeGraphView() {
    // Check if the component has valid dimensions before initializing
    if (getWidth() > 0 && getHeight() > 0 && !initialized) {
      Logging.INSTANCE.info("Initializing Knowledge Graph View");
      var initialPlacement = new SmartRandomPlacementStrategy();
      var graph = new DigraphEdgeList<RuntimeAsset, ClientKnowledgeGraph.Relationship>();
      this.graphView = new SmartGraphPanel<>(graph, initialPlacement);
      this.setCenter(this.graphView);
      this.graphView.setAutomaticLayout(true);

      graphView.setVertexDoubleClickAction(
          graphVertex -> {
            var asset = graphVertex.getUnderlyingVertex().element();
            if (asset instanceof Asset wrapper) {
              asset = wrapper.getDelegate();
            }
            this.editor.focusOnAsset(asset);
          });

      graphView.setEdgeDoubleClickAction(
          graphEdge -> {
            Logging.INSTANCE.info(
                "Edge contains element: " + graphEdge.getUnderlyingEdge().element());
            // dynamically change the style, can also be done for a vertex
            graphEdge.setStyleInline("-fx-stroke: black; -fx-stroke-width: 2;");
          });

      // Create default start and end times (current time and 1 hour later)
      long currentTimeMs = System.currentTimeMillis();
      long oneHourLaterMs = currentTimeMs + (3600000 * 2); // 2 hour in milliseconds
      // Create the timeline component
      timeline = new Timeline(currentTimeMs, oneHourLaterMs, TimeUnit.MINUTES, 1);
      this.setBottom(timeline);
      timeline.setVisible(false);

      // Initialize the graph view after it's been added to the scene
      Platform.runLater(
          () -> {
            try {
              graphView.init();
              this.initialized = true;
              if (focalAsset != null) {
                updateGraph(graphView.getModel(), focalAsset);
              }
            } catch (IllegalStateException e) {
              // If still not ready, try again after another layout pass
              Platform.runLater(
                  () -> {
                    if (graphView.getWidth() > 0 && graphView.getHeight() > 0) {
                      graphView.init();
                      this.initialized = true;
                      if (focalAsset != null) {
                        updateGraph(graphView.getModel(), focalAsset);
                      }
                    }
                  });
            }
          });
    } else if (!initialized) {
      // If dimensions are still not available, try again on the next pulse
      Platform.runLater(() -> initializeGraphView());
    }
  }

  public void setFocalAsset(RuntimeAsset asset) {
    focalAsset = asset;
    if (initialized) {
      updateGraph(graphView.getModel(), asset);
      graphView.update();
    }
  }

  public void clear() {
    for (var vertex : graphView.getModel().vertices()) {
      graphView.getModel().removeVertex(vertex);
    }
  }

  private void fillGraph(
      Graph<RuntimeAsset, ClientKnowledgeGraph.Relationship> graph,
      Asset asset,
      int depth,
      Set<Asset> cache) {

    for (GraphModel.Relationship relationship : relationships) {
      for (var targetEdge : knowledgeGraph.getGraph().outgoingEdgesOf(asset.getDelegate())) {
        if (this.relationships.contains(targetEdge.relationship)) {
          var target = knowledgeGraph.getGraph().getEdgeTarget(targetEdge);
          var targetAsset = new Asset(target);
          if (!cache.contains(targetAsset)) {
            graph.insertVertex(targetAsset);
            cache.add(targetAsset);
          }
          graph.insertEdge(asset, targetAsset, targetEdge);
          if (depth > 1) {
            fillGraph(graph, targetAsset, depth - 1, cache);
          }
        }
      }
    }
  }

  public void updateGraph(
      Graph<RuntimeAsset, ClientKnowledgeGraph.Relationship> graph, RuntimeAsset asset) {
    var focus = knowledgeGraph.getAsset(asset.getId());
    clear();
    var cache = new HashSet<Asset>();
    var focusAsset = new Asset(focus);
    cache.add(focusAsset);
    this.autoLayout = true;
    graph.insertVertex(focusAsset);
    fillGraph(this.graphView.getModel(), focusAsset, depth, cache);
    this.graphView.update();
    Platform.runLater(
        () -> {
          timeline.drawTimeline();
          for (var graphAsset : cache) {
            graphAsset.setStyle(this.graphView);
          }
        });
  }

  @Override
  public void submissionStarted(Observation observation) {}

  @Override
  public void submissionAborted(Observation observation) {}

  @Override
  public void submissionFinished(Observation observation) {
    setFocalAsset(observation);
  }

  @Override
  public void setContext(Observation observation) {}

  @Override
  public void setObserver(Observation observation) {}

//  @Override
//  public void activityFinished(Activity activity) {}
//
//  @Override
//  public void activityStarted(Activity activity) {}
//
//  @Override
//  public void knowledgeGraphCommitted(GraphModel.KnowledgeGraph graph) {}


  @Override
  public void knowledgeGraphModified() {

  }

  @Override
  public void activitiesModified(org.jgrapht.Graph<Activity, DefaultEdge> activityGraph) {

  }

  @Override
  public void scheduleModified(Schedule schedule) {
    if (!timeline.isVisible()) {
      timeline.setVisible(true);
    }
    timeline.updateEndTime(schedule.getEnd());
  }

  @Override
  public void cleanup() {}
}
