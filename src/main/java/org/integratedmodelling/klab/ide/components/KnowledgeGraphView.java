package org.integratedmodelling.klab.ide.components;

import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.GraphEdgeList;
import com.brunomnsilva.smartgraph.graphview.SmartCircularSortedPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import java.util.EnumSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.services.client.digitaltwin.ClientKnowledgeGraph;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.scope.ContextScope;

public class KnowledgeGraphView extends BorderPane {

  private final ClientKnowledgeGraph knowledgeGraph;
  private final ContextScope scope;
  private SmartGraphPanel<RuntimeAsset, ClientKnowledgeGraph.Relationship> graphView;
  private int depth = 2;
  private Set<GraphModel.Relationship> relationships =
      EnumSet.of(GraphModel.Relationship.HAS_CHILD);
  private RuntimeAsset focalAsset = null;
  private boolean initialized;

  public KnowledgeGraphView(ContextScope scope, ClientKnowledgeGraph editor) {
    this.scope = scope;
    this.knowledgeGraph = editor;
    this.sceneProperty()
        .addListener(
            (observable, oldScene, newScene) -> {
              if (!initialized && newScene != null) {
                // Delay initialization until the next pulse to ensure proper layout
                Platform.runLater(() -> initializeGraphView());
              }
            });
  }

  private void initializeGraphView() {
    // Check if the component has valid dimensions before initializing
    if (getWidth() > 0 && getHeight() > 0 && !initialized) {
      Logging.INSTANCE.info("Initializing Knowledge Graph View");
      var initialPlacement = new SmartCircularSortedPlacementStrategy();
      var graph = new DigraphEdgeList<RuntimeAsset, ClientKnowledgeGraph.Relationship>();
      this.graphView = new SmartGraphPanel<>(graph, initialPlacement);
      this.setCenter(this.graphView);
      this.graphView.setAutomaticLayout(true);

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

  private void fillGraph(
      Graph<RuntimeAsset, ClientKnowledgeGraph.Relationship> graph, RuntimeAsset asset, int depth) {

    for (GraphModel.Relationship relationship : relationships) {
      for (var targetEdge : knowledgeGraph.getGraph().outgoingEdgesOf(asset)) {
        if (this.relationships.contains(targetEdge.relationship)) {
          var target = knowledgeGraph.getGraph().getEdgeTarget(targetEdge);
          graph.insertVertex(target);
          graph.insertEdge(asset, target, targetEdge);
          if (depth > 1) {
            fillGraph(graph, target, depth - 1);
          }
        }
      }
    }
  }

  public void updateGraph(
      Graph<RuntimeAsset, ClientKnowledgeGraph.Relationship> graph, RuntimeAsset asset) {
    var focus = knowledgeGraph.getAsset(asset.getId());
    graph.insertVertex(focus);
    fillGraph(this.graphView.getModel(), focus, depth);
    this.graphView.update();
  }
}
