package org.integratedmodelling.klab.ide.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resources;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.identities.UserIdentity;
import org.integratedmodelling.klab.api.services.KlabService;
import org.integratedmodelling.klab.api.view.modeler.panels.controllers.DistributionViewController;
import org.integratedmodelling.klab.api.view.modeler.views.AuthenticationView;
import org.integratedmodelling.klab.api.view.modeler.views.DistributionView;
import org.integratedmodelling.klab.api.view.modeler.views.ServicesView;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.AuthenticationViewController;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.ServicesViewController;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/** The basic controls to start, stop and monitor the engine and services. */
public class EngineView extends BorderPane
    implements ServicesView, AuthenticationView, DistributionView {

  @FXML private FontIcon distributionButton;
  @FXML private FontIcon powerButton;
  @FXML private FontIcon profileButton;
  @FXML private FontIcon settingsButton;

  ServicesViewController servicesController;
  AuthenticationViewController authenticationController;
  DistributionViewController distributionController;

  public EngineView() {
    var fxmlLoader = new FXMLLoader(getClass().getResource("engine-view.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @FXML
  protected void initialize() {
    this.servicesController =
        KlabIDEController.modeler().viewController(ServicesViewController.class);
    this.authenticationController =
        KlabIDEController.modeler().viewController(AuthenticationViewController.class);
    this.distributionController =
        KlabIDEController.modeler().viewController(DistributionViewController.class);

    this.servicesController.registerView(this);
    this.authenticationController.registerView(this);
    this.distributionController.registerView(this);

    distributionButton.setOnMouseClicked(
        mouseEvent -> {
          Thread.ofPlatform()
              .start(
                  () -> {
                    KlabIDEController.modeler().shutdown(true);
                    System.exit(0);
                  });
        });
    powerButton.setOnMouseClicked(
        mouseEvent -> {
          Thread.ofPlatform()
              .start(
                  () -> {
                    KlabIDEController.modeler().boot();
                    //                    Platform.exit();
                  });
        });
    profileButton.setOnMouseClicked(mouseEvent -> {});

    settingsButton.setOnMouseClicked(mouseEvent -> {});
  }

  @FXML
  protected void onPowerButtonClick() {}

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
  public void notifyUser(UserIdentity identity) {
//    System.out.println("USER DIOPORCO");
  }
}
