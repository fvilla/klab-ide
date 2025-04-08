package org.integratedmodelling.klab.ide;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.integratedmodelling.common.authentication.Authentication;
import org.integratedmodelling.common.utils.Utils;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.engine.distribution.Distribution;
import org.integratedmodelling.klab.api.identities.UserIdentity;
import org.integratedmodelling.klab.api.services.KlabService;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.services.runtime.Notification;
import org.integratedmodelling.klab.api.view.UI;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.api.view.modeler.views.AuthenticationView;
import org.integratedmodelling.klab.api.view.modeler.views.DistributionView;
import org.integratedmodelling.klab.api.view.modeler.views.ServicesView;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.AuthenticationViewController;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.DistributionViewController;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.ServicesViewController;
import org.integratedmodelling.klab.modeler.ModelerImpl;

/** The main UI. Should probably include an Engine view. */
public class KlabIDEController implements UI, ServicesView, AuthenticationView, DistributionView {

  private static Modeler modeler;

  @FXML Button homeButton;
  @FXML Button workspacesButton;
  @FXML Button digitalTwinsButton;
  @FXML Button helpButton;
  @FXML Button rightButton;
  @FXML Button leftButton;
  @FXML Button downloadButton;
  @FXML Button startButton;
  @FXML Button reasonerButton;
  @FXML Button resourcesButton;
  @FXML Button resolverButton;
  @FXML Button runtimeButton;
  @FXML Button settingsButton;
  @FXML Button inspectorButton;
  @FXML Button profileButton;

  @FXML Pane mainArea;
  @FXML Pane inspectorArea;
  @FXML Pane browsingArea;

  ServicesViewController servicesController;
  AuthenticationViewController authenticationController;
  DistributionViewController distributionController;
  private Distribution distribution;

  public KlabIDEController() {
    createModeler();
  }

  public static Modeler modeler() {
    return modeler;
  }

  private void createModeler() {
    modeler = new ModelerImpl(this);
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

  @FXML
  protected void initialize() {

//    this.distribution = Authentication.INSTANCE.getDistribution();

    this.servicesController =
        KlabIDEController.modeler().viewController(ServicesViewController.class);
    this.authenticationController =
        KlabIDEController.modeler().viewController(AuthenticationViewController.class);
    this.distributionController =
        KlabIDEController.modeler().viewController(DistributionViewController.class);

    this.servicesController.registerView(this);
    this.authenticationController.registerView(this);
    this.distributionController.registerView(this);

    startButton.setOnMouseClicked(
        mouseEvent -> {
          Thread.ofPlatform()
              .start(
                  () -> {
                    KlabIDEController.modeler().shutdown(true);
                    System.exit(0);
                  });
        });

    downloadButton.setOnMouseClicked(mouseEvent -> {});
    profileButton.setOnMouseClicked(mouseEvent -> {});
    settingsButton.setOnMouseClicked(mouseEvent -> {});

    // TODO this shouldn't start by itself
    Thread.ofPlatform()
        .start(
            () -> {
              KlabIDEController.modeler().boot();
            });
  }

  @Override
  public void servicesConfigurationChanged(KlabService.ServiceCapabilities service) {
    //    System.out.println("PUTAZZA CONFIG");
  }

  @Override
  public void notifyServiceStatus(KlabService.ServiceStatus status) {
    //    System.out.println("ZOBO " + status);
  }

  @Override
  public void engineStatusChanged(Engine.Status status) {
    System.out.println("ZUBO " + (status.isAvailable() ? "AVAILABLE" : "INCULENTO"));
  }

  @Override
  public void show() {}

  @Override
  public void hide() {}

  @Override
  public void enable() {}

  @Override
  public void disable() {}

  @Override
  public boolean isShown() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void notifyUser(UserIdentity identity) {}

  @Override
  public void notifyDistribution(Distribution distribution) {
    System.out.println("PORCO DIO LA DISTRIBUZIONE");
  }
}
