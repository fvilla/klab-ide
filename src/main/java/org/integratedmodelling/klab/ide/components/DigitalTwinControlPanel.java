package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.RingProgressIndicator;
import javafx.application.Platform;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.integratedmodelling.common.logging.Logging;
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
  private TreeTableView<?> treeTableView;
  /* controller is bound after the first observation is made */
  private DigitalTwinPeer controller;

  // otherwise?

  public DigitalTwinControlPanel(int size, EditorPage<?> editorPage) {
    super();
    setMinHeight(size);
    setMinWidth(size);

    treeTableView = new TreeTableView<>();
    treeTableView.setMinSize(220, 220);

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
  }

  public void setStatus(Status status) {
    this.status = status;
    Platform.runLater(
        () -> {
          switch (status) {
            case IDLE -> {
              setCenter(treeTableView);
            }
            case RECEIVING -> {
              setCenter(dropZone);
            }
            case COMPUTING -> {
              var progressIndicator = new RingProgressIndicator(-1);
              progressIndicator.setPrefSize(180, 180);
              setCenter(progressIndicator);
            }
          }
        });
  }

  public void setScope(ContextScope scope) {

    if (this.scope != null) {
      throw new KlabInternalErrorException("SCOPE CAN ONLY BE BOUND ONCE IN A DT WIDGET");
    }
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
    // update activity record
    Logging.INSTANCE.info( "Ciöciàat lé " + activity);
  }

  @Override
  public void activityStarted(Activity activity) {
    // add activity to the table
    Logging.INSTANCE.info( "Ciöcia lé" + activity);
  }

  @Override
  public void knowledgeGraphCommitted(RuntimeAssetGraph graph) {}

  @Override
  public void scheduleModified(Schedule schedule) {}

  @Override
  public void cleanup() {}
}
