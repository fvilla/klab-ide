package org.integratedmodelling.klab.ide.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.common.services.client.digitaltwin.ClientDigitalTwin;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.DescriptionType;
import org.integratedmodelling.klab.api.knowledge.SemanticType;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.integratedmodelling.klab.api.provenance.impl.ActivityImpl;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.runtime.Message;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * We register context scopes with the IDE and use this class to manage all {@link
 * org.integratedmodelling.klab.ide.api.DigitalTwinViewer} objects linked to it.
 *
 * <p>TODO this must store state and propagate to all newly registered widgets on registration. TODO
 * the event processing must be atomically synchronized with the registration
 */
public class DigitalTwinPeer {

  private final ContextScope scope;
  private final Set<DigitalTwinViewer> viewers = Collections.synchronizedSet(new HashSet<>());
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  private Graph<Activity, DefaultEdge> activityGraph =
      new DefaultDirectedGraph<>(DefaultEdge.class);
  private HashMap<Long, Activity> activities = new HashMap<>();
  private Schedule schedule;

  public DigitalTwinPeer(ContextScope scope) {
    this.scope = scope;
    if (scope.getDigitalTwin() instanceof ClientDigitalTwin clientDigitalTwin) {
      clientDigitalTwin.addEventConsumer(message -> executor.execute(() -> processEvent(message)));
    }
  }

  public ContextScope scope() {
    return scope;
  }

  private void processEvent(Message message) {

    // TODO process state internally and send more specific messages to the viewers

    switch (message.getMessageType()) {
      case KnowledgeGraphCommitted -> {
        //        var graph = message.getPayload(GraphModel.KnowledgeGraph.class);
        executor.execute(() -> viewers.forEach(v -> v.knowledgeGraphModified()));
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
        executor.execute(() -> viewers.forEach(v -> v.submissionFinished(observation)));
      }
      case ActivityFinished -> {
        var activity = message.getPayload(Activity.class);
        // TODO update the existing activity in the graph
        var existingActivity = activities.get(activity.getTransientId());
        if (existingActivity instanceof ActivityImpl impl) {
          impl.setEnd(activity.getEnd());
          impl.setOutcome(activity.getOutcome());
        }
        executor.execute(() -> viewers.forEach(v -> v.activitiesModified(activityGraph)));
      }
      case ActivityStarted -> {
        var activity = message.getPayload(Activity.class);
        activities.put(activity.getTransientId(), activity);
        activityGraph.addVertex(activity);
        var parentActivity =
            activity.getMetadata().get(ActivityImpl.PARENT_ACTIVITY_TRANSIENT_ID_KEY, -1L);
        if (parentActivity > 0) {
          var activityParent = activities.get(parentActivity);
          if (activityParent != null) {
            activityGraph.addEdge(activityParent, activity);
          }
        }
        executor.execute(() -> viewers.forEach(v -> v.activitiesModified(activityGraph)));
      }
      case ScheduleModified -> {
        this.schedule = message.getPayload(Schedule.class);
        executor.execute(() -> viewers.forEach(v -> v.scheduleModified(schedule)));
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

    // TODO load any existing state into the new viewer
    digitalTwinEditor.knowledgeGraphModified();
    digitalTwinEditor.activitiesModified(activityGraph);
    if (schedule != null) {
      digitalTwinEditor.scheduleModified(schedule);
    }
  }

  public void executeTask(Runnable task) {
    executor.execute(task);
  }

  public void cleanup() {
    executor.shutdown();
  }

  public void focus(RuntimeAsset asset) {
    if (asset instanceof Observation observation
        && observation.getObservable().is(SemanticType.COUNTABLE)
        && !observation.getObservable().getSemantics().isCollective()) {
      executor.execute(() -> viewers.forEach(v -> v.setContext(observation)));
    }
  }

  public void closeScope() {

    executor.submit(
        () -> {
          viewers.forEach(v -> {
            v.cleanup();
          });
          scope.close();
        });
    try {
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // wtf
    }
    cleanup();
  }
}
