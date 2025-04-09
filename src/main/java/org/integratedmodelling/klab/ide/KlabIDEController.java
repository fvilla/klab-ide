package org.integratedmodelling.klab.ide;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.integratedmodelling.common.authentication.Authentication;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.engine.distribution.Distribution;
import org.integratedmodelling.klab.api.engine.distribution.Product;
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
import org.integratedmodelling.klab.ide.components.IconLabel;
import org.integratedmodelling.klab.ide.settings.IDESettings;
import org.integratedmodelling.klab.modeler.ModelerImpl;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.weathericons.WeatherIcons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** The main UI. Should probably include an Engine view. */
public class KlabIDEController implements UI, ServicesView, AuthenticationView, DistributionView {

  private static Modeler modeler;
  private View currentView;

  /** The "circled" (current) view in the main area. */
  public enum View {
    NOTEBOOK,
    RESOURCES,
    WORKSPACES,
    DIGITAL_TWINS
  }

  @FXML Button homeButton;
  @FXML Button workspacesButton;
  @FXML Button digitalTwinsButton;
  @FXML Button downloadButton;
  @FXML Button startButton;
  @FXML Button reasonerButton;
  @FXML Button resourcesButton;
  @FXML Button resolverButton;
  @FXML Button runtimeButton;
  @FXML Button settingsButton;
  @FXML Button inspectorButton;
  @FXML Button profileButton;
  @FXML Button resourcesManagerButton;

  @FXML Pane mainArea;
  @FXML Pane inspectorArea;
  @FXML Pane browsingArea;

  private ServicesViewController servicesController;
  private AuthenticationViewController authenticationController;
  private DistributionViewController distributionController;
  private Distribution distribution;
  private IDESettings settings;
  private Map<View, Button> viewButtons = new HashMap<>();

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

  public void selectView(View view) {
    this.currentView = view;
    for (var v : viewButtons.keySet()) {
      var button = viewButtons.get(v);
      if (v == view) {
        button.getStyleClass().removeAll(Styles.FLAT);
        button.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.BUTTON_OUTLINED);
      } else {
        button.getStyleClass().removeAll(Styles.BUTTON_OUTLINED);
        button.getStyleClass().addAll(Styles.BUTTON_CIRCLE, Styles.FLAT);
      }
    }
    // TODO switch main panel to the view!
  }

  public View selectedView() {
    return this.currentView;
  }

  @FXML
  protected void initialize() {

    this.settings = new IDESettings();

    this.servicesController =
        KlabIDEController.modeler().viewController(ServicesViewController.class);
    this.authenticationController =
        KlabIDEController.modeler().viewController(AuthenticationViewController.class);
    this.distributionController =
        KlabIDEController.modeler().viewController(DistributionViewController.class);

    this.servicesController.registerView(this);
    this.authenticationController.registerView(this);
    this.distributionController.registerView(this);

    // painful and should not be necessary
    // TODO sync with theme from application
    homeButton.setGraphic(
        new IconLabel(FontAwesomeSolid.HOME, 24, Theme.LIGHT_DEFAULT.getDefaultTextColor()));
    workspacesButton.setGraphic(new IconLabel(BootstrapIcons.BORDER_ALL, 24, Color.GREY));
    resourcesManagerButton.setGraphic(new IconLabel(FontAwesomeSolid.CUBES, 24, Color.GREY));
    digitalTwinsButton.setGraphic(new IconLabel(WeatherIcons.EARTHQUAKE, 24, Color.GREY));
    downloadButton.setGraphic(new IconLabel(Material2AL.GET_APP, 24, Color.GREY));
    startButton.setGraphic(new IconLabel(Material2MZ.POWER_SETTINGS_NEW, 24, Color.GREY));
    reasonerButton.setGraphic(new IconLabel(Material2AL.BLUR_ON, 24, Theme.REASONER_COLOR_MUTED));
    resourcesButton.setGraphic(new IconLabel(Material2AL.BLUR_ON, 24, Theme.RESOURCES_COLOR_MUTED));
    resolverButton.setGraphic(new IconLabel(Material2AL.BLUR_ON, 24, Theme.RESOLVER_COLOR_MUTED));
    runtimeButton.setGraphic(new IconLabel(Material2AL.BLUR_ON, 24, Theme.RUNTIME_COLOR_MUTED));
    settingsButton.setGraphic(new IconLabel(FontAwesomeSolid.COG, 24, Color.DARKBLUE));
    inspectorButton.setGraphic(new IconLabel(FontAwesomeSolid.LIGHTBULB, 24, Color.DARKGOLDENROD));
    profileButton.setGraphic(new IconLabel(FontAwesomeRegular.USER_CIRCLE, 24, Color.GREY));

    viewButtons.put(View.NOTEBOOK, homeButton);
    viewButtons.put(View.DIGITAL_TWINS, digitalTwinsButton);
    viewButtons.put(View.RESOURCES, resourcesManagerButton);
    viewButtons.put(View.WORKSPACES, workspacesButton);

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

    for (var key : viewButtons.keySet()) {
      viewButtons.get(key).setOnMouseClicked(mouseEvent -> selectView(key));
    }

//    selectView(View.NOTEBOOK);

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
    //    System.out.println("ZUBO " + (status.isAvailable() ? "AVAILABLE" : "INCULENTO"));
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

    Ikon icon = Material2AL.BLUR_ON;
    var color = Color.GREEN;
    var status = Authentication.INSTANCE.getDistributionStatus();
    var tooltip = "Wait hostia";

    if (status.getDevelopmentStatus() == Product.Status.UP_TO_DATE
        && "source".equals(settings.getPrimaryDistribution().getValue())) {

      this.distribution = distribution;
      icon = BootstrapIcons.LAPTOP;
      tooltip = "Using locally available source k.LAB distribution";

    } else {
      switch (status.getDownloadedStatus()) {
        case UNAVAILABLE -> {
          color = Color.RED;
          tooltip = "No distribution available. Click to download";
          icon = Material2AL.GET_APP;
        }
        case LOCAL_ONLY -> {}
        case UP_TO_DATE -> {}
        case OBSOLETE -> {
          color = Color.GOLDENROD;
          tooltip = "Updated k.LAB distribution available. Click to update";
          icon = Material2AL.GET_APP;
        }
      }
    }

    setButton(downloadButton, icon, 24, color, tooltip);
    // TODO set the ON button as needed if not starting automatically
  }

  public static void setButton(Button button, Ikon icon, int size, Color color, String tooltip) {
    var iconControl = button.getGraphic();
    if (iconControl instanceof IconLabel fontIcon) {
      Platform.runLater(
          () -> {
            fontIcon.set(icon, size, color);
            var ttp = new Tooltip(tooltip);
            ttp.setShowDelay(Duration.millis(200));
            button.setTooltip(ttp);
          });
    }
  }
}
