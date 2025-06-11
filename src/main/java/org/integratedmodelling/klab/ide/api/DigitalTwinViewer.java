package org.integratedmodelling.klab.ide.api;

import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

public interface DigitalTwinViewer {

  void submissionStarted(Observation observation);

  void submissionAborted(Observation observation);

  void submissionFinished(Observation observation);

  void setContext(Observation observation);

  void setObserver(Observation observation);

  //  void activityFinished(Activity activity);
  //
  //  void activityStarted(Activity activity);

  void knowledgeGraphModified();

  void scheduleModified(Schedule schedule);

  void cleanup();

  /**
   * The graph will contain all the activities seen during contextualization, arranged
   * hierarchically. The hierarchy is based on transient information collected during resolution,
   * not related to the provenance graph structure (it must be reconstructed from the metadata if
   * restored from the knowledge graph).
   *
   * TODO to use: collect the root observations, sort by start or end, display as needed.
   *
   * @param activityGraph
   */
  void activitiesModified(Graph<Activity, DefaultEdge> activityGraph);
}
