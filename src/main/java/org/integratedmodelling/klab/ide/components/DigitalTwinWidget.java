package org.integratedmodelling.klab.ide.components;

import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.ide.api.DigitalTwinViewer;
import org.integratedmodelling.klab.ide.pages.EditorPage;
import org.kordamp.ikonli.material2.Material2AL;

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
public class DigitalTwinWidget extends BorderPane implements DigitalTwinViewer {

  public DigitalTwinWidget(int size, EditorPage<?> editorPage) {
    super();
    setMinHeight(size);
    setMinWidth(size);
    setCenter(new IconLabel(Material2AL.ARROW_CIRCLE_DOWN, 220, Color.DARKGREEN));

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
