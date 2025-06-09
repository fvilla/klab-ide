package org.integratedmodelling.klab.ide.components;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.digitaltwin.GraphModel;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.knowledge.observation.scale.time.Schedule;
import org.integratedmodelling.klab.api.provenance.Activity;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;

public class KnowledgeGraphTree extends TreeView<RuntimeAsset> implements DigitalTwinViewer {

  public KnowledgeGraphTree() {
    super();
  }

  public KnowledgeGraphTree(TreeItem<RuntimeAsset> runtimeAssetTreeItem) {
    super(runtimeAssetTreeItem);
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
  public void activityFinished(Activity activity) {}

  @Override
  public void activityStarted(Activity activity) {}

  @Override
  public void knowledgeGraphCommitted(GraphModel.KnowledgeGraph graph) {}

  @Override
  public void scheduleModified(Schedule schedule) {}

  @Override
  public void cleanup() {}
}
