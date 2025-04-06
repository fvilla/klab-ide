package org.integratedmodelling.klab.ide;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.services.runtime.Notification;
import org.integratedmodelling.klab.api.view.UI;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.modeler.ModelerImpl;

/** The main UI. Should probably include an Engine view. */
public class KlabIDEController implements UI {

  private static Modeler modeler;

//  @FXML private Label welcomeText;

  public KlabIDEController() {
    createModeler();
  }

  public static Modeler modeler() {
    return modeler;
  }

  private void createModeler() {
    modeler = new ModelerImpl(this);
  }

//  @FXML
  protected void onHelloButtonClick() {
//    welcomeText.setText("Shutting down...");
    Thread.ofPlatform()
        .start(
            () -> {
              modeler().shutdown(true);
//              Platform.exit();
            });
  }



  @Override
  public void alert(Notification notification) {
    var alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText("Notification");
    alert.setContentText(notification.getMessage());

    ButtonType yesBtn = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    ButtonType noBtn = new ButtonType("No", ButtonBar.ButtonData.NO);
    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    alert.getButtonTypes().setAll(yesBtn, noBtn, cancelBtn);
//    alert.initOwner(welcomeText.getScene().getWindow());
    alert.show();
  }

  @Override
  public boolean confirm(Notification notification) {
    return false;
  }

  @Override
  public void log(Notification notification) {}

  @Override
  public void cleanWorkspace() {}

  @Override
  public ResourceSet processAlerts(ResourceSet resourceSet) {
    return null;
  }
}
