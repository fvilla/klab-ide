package org.integratedmodelling.klab.ide.components;

import jakarta.annotation.PostConstruct;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
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

/**
 * The basic controls to start, stop and monitor the engine and services.
 */
public class EngineView extends AnchorPane
    implements ServicesView, AuthenticationView, DistributionView {

  ServicesViewController servicesController;
  AuthenticationViewController authenticationController;
  DistributionViewController distributionController;

  public EngineView() {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("components/engine-view.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(EngineView.this);
  }

  @PostConstruct
  private void initialize() {
    this.servicesController =
            KlabIDEController.modeler().viewController(ServicesViewController.class);
    this.authenticationController =
            KlabIDEController.modeler().viewController(AuthenticationViewController.class);
    this.distributionController =
            KlabIDEController.modeler().viewController(DistributionViewController.class);

    this.servicesController.registerView(this);
    this.authenticationController.registerView(this);
    this.distributionController.registerView(this);

  }

  @Override
  public void servicesConfigurationChanged(KlabService.ServiceCapabilities service) {}

  @Override
  public void notifyServiceStatus(KlabService.ServiceStatus status) {}

  @Override
  public void engineStatusChanged(Engine.Status status) {}

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
    return false;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void notifyUser(UserIdentity identity) {}
}
