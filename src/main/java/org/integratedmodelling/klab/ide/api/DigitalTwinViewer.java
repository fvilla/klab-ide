package org.integratedmodelling.klab.ide.api;

import org.integratedmodelling.klab.api.data.RuntimeAssetGraph;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;

public interface DigitalTwinViewer {

  void submissionStarted(Observation observation);

  void submissionAborted(Observation observation);

  void submissionFinished(Observation observation);

  void setContext(Observation observation);

  void setObserver(Observation observation);

  void activityFinished(Activity activity);

  void activityStarted(Activity activity);

  void knowledgeGraphCommitted(RuntimeAssetGraph graph);

  void scheduleModified(Schedule schedule);

  void cleanup();
}
