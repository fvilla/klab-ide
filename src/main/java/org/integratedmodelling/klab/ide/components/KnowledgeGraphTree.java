package org.integratedmodelling.klab.ide.components;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;

public class KnowledgeGraphTree extends TreeView<RuntimeAsset> implements DigitalTwinViewer {

  public KnowledgeGraphTree() {
    super();
  }

  public KnowledgeGraphTree(TreeItem<RuntimeAsset> runtimeAssetTreeItem) {
    super(runtimeAssetTreeItem);
  }

  @Override
  public void submission(Observation observation) {}

  @Override
  public void submissionAborted(Observation observation) {}

  @Override
  public void submissionFinished(Observation observation) {}

  @Override
  public void setContext(Observation observation) {}

  @Override
  public void setObserver(Observation observation) {}
}
