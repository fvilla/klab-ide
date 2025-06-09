package org.integratedmodelling.klab.ide.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.integratedmodelling.common.services.client.digitaltwin.ClientDigitalTwin;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.runtime.Message;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.integratedmodelling.klab.ide.components.KnowledgeGraphView;

/**
 * We register context scopes with the IDE and use this class to manage all {@link
 * org.integratedmodelling.klab.ide.api.DigitalTwinViewer} objects linked to it.
 */
public class DigitalTwinPeer {

  private final ContextScope scope;
  private final Set<DigitalTwinViewer> viewers = Collections.synchronizedSet(new HashSet<>());

  private RuntimeAsset context = RuntimeAsset.CONTEXT_ASSET;
  private KnowledgeGraphView knowledgeGraphView;

  public DigitalTwinPeer(ContextScope scope) {
    this.scope = scope;
    if (scope.getDigitalTwin() instanceof ClientDigitalTwin clientDigitalTwin) {
      clientDigitalTwin.addEventConsumer(this::processEvent);
    }
  }

  public ContextScope scope() {
    return scope;
  }

  private void processEvent(Message message) {

    switch (message.getMessageType()) {
      case KnowledgeGraphCommitted -> {
        var graph = message.getPayload(GraphModel.KnowledgeGraph.class);
        viewers.forEach(v -> v.knowledgeGraphCommitted(graph));
      }
      case ContextualizationAborted, ContextualizationSuccessful, ContextualizationStarted -> {
        // TODO insert object, define aspect
        //        if (message.getMessageType() == Message.MessageType.ContextualizationStarted) {
        //          knowledgeGraphView.setFocalAsset(message.getPayload(Observation.class));
        //        }
      }
      // FIXME sketchy logics
      case ObservationSubmissionAborted -> {}
      case ObservationSubmissionStarted -> {}
      case ObservationSubmissionFinished -> {
        var observation = message.getPayload(Observation.class);
        viewers.forEach(v -> v.submissionFinished(observation));
      }
      case ActivityFinished -> {
        var activity = message.getPayload(Activity.class);
        viewers.forEach(v -> v.activityFinished(activity));
      }
      case ActivityStarted -> {
        var activity = message.getPayload(Activity.class);
        viewers.forEach(v -> v.activityStarted(activity));
      }
      case ScheduleModified -> {
        var schedule = message.getPayload(Schedule.class);
        viewers.forEach(v -> v.scheduleModified(schedule));
      }
    }

    // TODO send to all sub-editors, widgets and the like
  }

  /**
   * Register a digital twin viewer. If the viewer extends {@link Pane} then it will be unregistered
   * automatically after calling its cleanup() function when the component is removed from the
   * scene. Otherwise be sure to unregister it manually.
   *
   * @param digitalTwinEditor
   */
  public void register(DigitalTwinViewer digitalTwinEditor) {
    this.viewers.add(digitalTwinEditor);
    if (digitalTwinEditor instanceof Node pane) {
      // unregister self and the knowledge tree on destruction
      pane.parentProperty()
          .addListener(
              (observable, oldParent, newParent) -> {
                if (oldParent != null && newParent == null) {
                  // Pane was removed from its parent
                  digitalTwinEditor.cleanup();
                  this.viewers.remove(digitalTwinEditor);
                }
              });
    }
  }
}
