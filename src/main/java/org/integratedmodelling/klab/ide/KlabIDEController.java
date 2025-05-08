package org.integratedmodelling.klab.ide;

import atlantafx.base.theme.Styles;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.klab.api.engine.Engine;
import org.integratedmodelling.klab.api.engine.distribution.Distribution;
import org.integratedmodelling.klab.api.engine.distribution.Product;
import org.integratedmodelling.klab.api.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.api.identities.UserIdentity;
import org.integratedmodelling.klab.api.scope.UserScope;
import org.integratedmodelling.klab.api.services.KlabService;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.RuntimeService;
import org.integratedmodelling.klab.api.services.resources.ResourceSet;
import org.integratedmodelling.klab.api.services.runtime.Notification;
import org.integratedmodelling.klab.api.utils.Utils;
import org.integratedmodelling.klab.api.view.UIView;
import org.integratedmodelling.klab.api.view.modeler.Modeler;
import org.integratedmodelling.klab.api.view.modeler.views.AuthenticationView;
import org.integratedmodelling.klab.api.view.modeler.views.DistributionView;
import org.integratedmodelling.klab.api.view.modeler.views.ServicesView;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.AuthenticationViewController;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.DistributionViewController;
import org.integratedmodelling.klab.api.view.modeler.views.controllers.ServicesViewController;
import org.integratedmodelling.klab.ide.components.*;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;
import org.integratedmodelling.klab.ide.settings.IDESettings;
import org.integratedmodelling.klab.ide.utils.NodeUtils;
import org.integratedmodelling.klab.modeler.ModelerImpl;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

/** The main UI. Should probably include an Engine view. */
public class KlabIDEController
    implements UIView, ServicesView, AuthenticationView, DistributionView {

  private static Modeler modeler;
  private View currentView;
  private UserScope user;
  private Map<KlabService.Type, KlabService.ServiceCapabilities> capabilities = new HashMap<>();
  private boolean inspectorIsOn;
  private Set<View> neverSeen = EnumSet.of(View.RESOURCES, View.WORKSPACES, View.DIGITAL_TWINS);
  private static KlabIDEController _this;

  /** The "circled" (current) view in the main area. */
  public enum View {
    NOTEBOOK,
    RESOURCES,
    WORKSPACES,
    DIGITAL_TWINS,
    APPLICATIONS,
    WORLDVIEW
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
  @FXML Button sessionsButton;
  @FXML Button worldviewButton;

  @FXML NotebookView notebook;
  @FXML Pane mainArea;
  @FXML Pane inspectorArea;
  //  @FXML Pane browsingArea;

  private ServicesViewController servicesController;
  private AuthenticationViewController authenticationController;
  private DistributionViewController distributionController;
  private Distribution distribution;
  private IDESettings settings;
  private Map<View, Button> viewButtons = new HashMap<>();
  private AtomicBoolean engineStarted = new AtomicBoolean(false);
  private AtomicBoolean engineTransitioning = new AtomicBoolean(false);

  private WorkspaceView workspaceView;
  private ResourcesView resourcesView;
  private DigitalTwinView digitalTwinView;
  private InspectorView inspectorView;
  private SessionView applicationView;
  private OntologyView ontologyView;

  public KlabIDEController() {
    _this = this;
    createModeler();
  }

  public static KlabIDEController instance() {
    return _this;
  }

  public static Modeler modeler() {
    return modeler;
  }

  private void createModeler() {

    modeler = new ModelerImpl(this);

    this.settings = new IDESettings();

    this.servicesController = modeler.viewController(ServicesViewController.class);
    this.authenticationController = modeler.viewController(AuthenticationViewController.class);
    this.distributionController = modeler.viewController(DistributionViewController.class);

    this.servicesController.registerView(this);
    this.authenticationController.registerView(this);
    this.distributionController.registerView(this);

    digitalTwinView = new DigitalTwinView();
    workspaceView = new WorkspaceView();
    resourcesView = new ResourcesView();
    inspectorView = new InspectorView();
    applicationView = new SessionView();
    ontologyView = new OntologyView();
  }

  @Override
  public void alert(Notification notification) {
    var alert =
        new Alert(
            switch (notification.getLevel()) {
              case Debug, Info -> Alert.AlertType.INFORMATION;
              case Notification.Level.Warning -> Alert.AlertType.WARNING;
              case Error, SystemError -> Alert.AlertType.ERROR;
            }); 
    alert.setTitle("Notification");
    alert.setHeaderText("Alert");
    alert.setContentText(notification.getMessage());
    alert.initOwner(KlabIDEApplication.scene().getWindow());
    alert.initStyle(StageStyle.DECORATED);
    alert.showAndWait();
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

    var ui =
        switch (view) {
          case NOTEBOOK -> notebook;
          case RESOURCES -> resourcesView;
          case DIGITAL_TWINS -> digitalTwinView;
          case WORKSPACES -> workspaceView;
          case APPLICATIONS -> applicationView;
          case WORLDVIEW -> ontologyView;
        };

    // If it's a browser and it hasn't been seen yet, open the browser
    if (neverSeen.remove(view) && ui instanceof BrowsablePage<?> browsablePage) {
      //      browsablePage.showBrowser();
    }

    // switch the main area to the requested view.
    Platform.runLater(
        () -> {
          mainArea.getChildren().remove(0, mainArea.getChildren().size());
          mainArea.getChildren().add(ui);
        });
  }

  public View selectedView() {
    return this.currentView;
  }

  @FXML
  protected void initialize() {

    modeler.boot();

    homeButton.setGraphic(
        new IconLabel(Material2AL.HOME, 24, Theme.CURRENT_THEME.getDefaultTextColor()));
    workspacesButton.setGraphic(new IconLabel(Theme.WORKSPACES_ICON, 24, Color.GREY));
    resourcesManagerButton.setGraphic(new IconLabel(Theme.RESOURCES_ICON, 24, Color.GREY));
    digitalTwinsButton.setGraphic(new IconLabel(Theme.DIGITAL_TWINS_ICON, 24, Color.GREY));
    sessionsButton.setGraphic(new IconLabel(Theme.APPLICATION_VIEW_ICON, 24, Color.GREY));
    worldviewButton.setGraphic(new IconLabel(Theme.WORLDVIEW_ICON, 24, Color.GREY));
    downloadButton.setGraphic(new IconLabel(Material2AL.GET_APP, 24, Color.GREY));
    startButton.setGraphic(new IconLabel(Material2MZ.POWER_SETTINGS_NEW, 32, Color.GREY));
    reasonerButton.setGraphic(new IconLabel(Theme.LOCAL_SERVICE_ICON, 16, Color.GREY));
    resourcesButton.setGraphic(new IconLabel(Theme.LOCAL_SERVICE_ICON, 16, Color.GREY));
    resolverButton.setGraphic(new IconLabel(Theme.LOCAL_SERVICE_ICON, 16, Color.GREY));
    runtimeButton.setGraphic(new IconLabel(Theme.LOCAL_SERVICE_ICON, 16, Color.GREY));
    settingsButton.setGraphic(
        new IconLabel(FontAwesomeSolid.COG, 24, Theme.CURRENT_THEME.getDefaultTextColor()));
    inspectorButton.setGraphic(
        new IconLabel(Theme.INSPECTOR_ICON, 24, Theme.CURRENT_THEME.getDefaultTextColor()));
    profileButton.setGraphic(new IconLabel(FontAwesomeSolid.USER_CIRCLE, 32, Color.GREY));

    viewButtons.put(View.NOTEBOOK, homeButton);
    viewButtons.put(View.DIGITAL_TWINS, digitalTwinsButton);
    viewButtons.put(View.RESOURCES, resourcesManagerButton);
    viewButtons.put(View.APPLICATIONS, sessionsButton);
    viewButtons.put(View.WORKSPACES, workspacesButton);
    viewButtons.put(View.WORLDVIEW, worldviewButton);

    inspectorButton.setOnMouseClicked(
        event -> {
          toggleInspector();
        });

    startButton.setOnMouseClicked(
        mouseEvent -> {
          if (engineTransitioning.get()) {
            Toolkit.getDefaultToolkit().beep();
            return;
          }

          Thread.ofPlatform()
              .start(
                  () -> {
                    engineTransitioning.set(true);
                    if (engineStarted.get()) {
                      engineStarted.set(false);
                      setButton(
                          startButton,
                          Material2AL.ACCESS_TIME,
                          32,
                          Color.DARKGOLDENROD,
                          "Wait while the services are stopping");
                      KlabIDEController.modeler().engine().stopLocalServices();
                      setButton(
                          startButton,
                          Material2MZ.POWER_SETTINGS_NEW,
                          32,
                          Color.GREEN,
                          "Click to start the local k.LAB services");
                    } else {
                      setButton(
                          startButton,
                          Material2AL.ACCESS_TIME,
                          32,
                          Color.DARKGOLDENROD,
                          "Wait while the services are starting");
                      engineStarted.set(true);
                      KlabIDEController.modeler().engine().startLocalServices();
                    }
                    engineTransitioning.set(false);
                  });
        });

    downloadButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.Distribution);
          selectView(View.NOTEBOOK);
        });
    profileButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.UserInfo);
          selectView(View.NOTEBOOK);
        });
    settingsButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.Settings);
          selectView(View.NOTEBOOK);
        });
    reasonerButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.ServiceInfo, KlabService.Type.REASONER);
          selectView(View.NOTEBOOK);
        });
    resourcesButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.ServiceInfo, KlabService.Type.RESOURCES);
          selectView(View.NOTEBOOK);
        });
    resolverButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.ServiceInfo, KlabService.Type.RESOLVER);
          selectView(View.NOTEBOOK);
        });
    runtimeButton.setOnMouseClicked(
        mouseEvent -> {
          notebook.focus(Components.Type.ServiceInfo, KlabService.Type.RUNTIME);
          selectView(View.NOTEBOOK);
        });

    for (var key : viewButtons.keySet()) {
      viewButtons.get(key).setOnMouseClicked(mouseEvent -> selectView(key));
    }

    this.user = modeler.authenticate();

    // must call explicitly because the callback won't be used before boot.
    notifyUser(this.user.getUser());
    notifyDistribution(modeler().getDistribution());

    if (settings.getStartServicesOnStartup().getValue()) {
      // TODO
      //      Thread.ofPlatform().start(this::toggleLocalServices);
    }
  }

  private void toggleInspector() {

    setButton(
        inspectorButton,
        Theme.INSPECTOR_ICON,
        24,
        inspectorIsOn ? Theme.CURRENT_THEME.getDefaultTextColor() : Color.GOLDENROD,
        inspectorIsOn
            ? "Click to show the knowledge inspector"
            : "Click to hide the knowledge inspector");

    Platform.runLater(
        () -> {
          if (inspectorIsOn) {
            inspectorArea.getChildren().removeAll(inspectorView);
            inspectorIsOn = false;
            KlabIDEApplication.instance().setInspectorShown(false);
            NodeUtils.toggleVisibility(inspectorArea, false);
          } else {
            inspectorArea.getChildren().add(inspectorView);
            inspectorIsOn = true;
            KlabIDEApplication.instance().setInspectorShown(true);
            NodeUtils.toggleVisibility(inspectorArea, true);
          }
        });
  }

  public InspectorView getInspector() {
    return inspectorView;
  }

  /**
   * If single service in the cloud, use BootstrapIcons.CLOUDY_FILL If multiple services in the
   * cloud, use BootstrapIcons.CLOUDS_FILL If local service, use Material2AL.DONUT_SMALL All with
   * the color from Theme
   *
   * @param user
   */
  private void checkServices(UserScope user, Engine.Status status) {

    for (var serviceType :
        List.of(
            KlabService.Type.RESOURCES,
            KlabService.Type.REASONER,
            KlabService.Type.RUNTIME,
            KlabService.Type.RESOLVER)) {

      String serviceName = serviceType.name().toLowerCase();
      Ikon icon = Theme.LOCAL_SERVICE_ICON;
      var service = user.getService(serviceType.classify());
      String tooltip = Utils.Strings.capitalize(serviceName) + " ";

      if (service != null)
        if (!Utils.URLs.isLocalHost(service.getUrl())) {
          tooltip = "Remote " + serviceName + " " + service.getServiceName();
          icon =
              user.getServices(serviceType.classify()).size() > 1
                  ? Theme.REMOTE_SERVICE_ICON_MANY
                  : Theme.REMOTE_SERVICE_ICON_ONE;
        } else {
          tooltip = "Local " + serviceName;
        }

      var button =
          switch (serviceType) {
            case REASONER -> reasonerButton;
            case RESOURCES -> resourcesButton;
            case RESOLVER -> resolverButton;
            case RUNTIME -> runtimeButton;
            default -> throw new KlabInternalErrorException("?"); // can't happen
          };

      var color =
          switch (serviceType) {
            case REASONER ->
                engineStarted.get() ? Theme.REASONER_COLOR_ACTIVE : Theme.REASONER_COLOR_MUTED;
            case RESOURCES ->
                engineStarted.get() ? Theme.RESOURCES_COLOR_ACTIVE : Theme.RESOURCES_COLOR_MUTED;
            case RESOLVER ->
                engineStarted.get() ? Theme.RESOLVER_COLOR_ACTIVE : Theme.RESOLVER_COLOR_MUTED;
            case RUNTIME ->
                engineStarted.get() ? Theme.RUNTIME_COLOR_ACTIVE : Theme.RUNTIME_COLOR_MUTED;
            default -> throw new KlabInternalErrorException("?"); // can't happen
          };

      if (serviceType == KlabService.Type.RESOURCES
          && user.getServices(ResourcesService.class).stream()
              .anyMatch(s -> s.status().isOperational())) {
        setButton(
            workspacesButton,
            Theme.RESOURCES_ICON,
            24,
            Color.DARKGREEN,
            workspacesButton.getTooltip().getText());
        setButton(
            resourcesManagerButton,
            Theme.RESOURCES_ICON,
            24,
            Color.DARKGREEN,
            resourcesManagerButton.getTooltip().getText());
      } else {
        setButton(
            workspacesButton,
            Theme.WORKSPACES_ICON,
            24,
            Color.GREY,
            workspacesButton.getTooltip().getText());
        setButton(
            resourcesManagerButton,
            Theme.RESOURCES_ICON,
            24,
            Color.GREY,
            resourcesManagerButton.getTooltip().getText());
      }

      if (serviceType == KlabService.Type.RUNTIME
          && user.getServices(RuntimeService.class).stream()
              .anyMatch(s -> s.status().isOperational())) {
        setButton(
            digitalTwinsButton,
            Theme.DIGITAL_TWINS_ICON,
            24,
            Color.DARKRED,
            digitalTwinsButton.getTooltip().getText());
      } else {
        setButton(
            digitalTwinsButton,
            Theme.DIGITAL_TWINS_ICON,
            24,
            Color.GREY,
            digitalTwinsButton.getTooltip().getText());
      }

      setButton(button, icon, 16, color, tooltip);
    }
  }

  @Override
  public void servicesConfigurationChanged(KlabService.ServiceCapabilities service) {
    this.capabilities.put(service.getType(), service);
  }

  @Override
  public void notifyServiceStatus(KlabService.ServiceStatus status) {
    /* won't happen automatically - should be deprecated */

  }

  @Override
  public void serviceFocusChanged(KlabService.ServiceCapabilities serviceCapabilities) {
    Logging.INSTANCE.info("PUTO SERVIZIO CHANGED " + serviceCapabilities);
  }

  @Override
  public void engineStatusChanged(Engine.Status status) {
    // This only gets called when the status has changed.
    Logging.INSTANCE.info("" + status);
    if (status.isAvailable()) {
      setButton(startButton, Material2MZ.STOP, 32, Color.DARKRED, "Click to stop the services");
      this.engineStarted.set(true);
    }

    for (var s : status.getServicesStatus().values()) {
      notifyServiceStatus(s);
    }

    /**
     * Services: absent or !available -> grey; available && !operational -> clock; available &&
     * operational -> icon; Engine on/off: all services operational -> stop; no service or none
     * available -> on; anything else -> wait;
     */
    checkServices(modeler().user(), status);

    this.workspaceView.updateServices(status);
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

    if (identity.isAnonymous()) {
      setButton(
          profileButton,
          FontAwesomeSolid.USER_CIRCLE,
          32,
          Color.DARKRED,
          "Anonymous user. Please obtain a certificate.");
    } else if (identity.isAuthenticated()) {
      setButton(
          profileButton,
          FontAwesomeSolid.USER_CIRCLE,
          32,
          Color.DARKGREEN,
          "User " + identity.getUsername() + " logged in");
    } else {
      setButton(
          profileButton,
          FontAwesomeSolid.USER_CIRCLE,
          32,
          Color.DARKGOLDENROD,
          "Authentication failed for user " + identity.getUsername());
    }

    /*
    TODO set the service icons to the color and icon for the services currently available after authentication.
     They can be local or remote, should have different icons and service-dependent colors.
     */

  }

  @Override
  public void notifyDistribution(Distribution distribution) {

    Ikon icon = BootstrapIcons.DOWNLOAD;
    var color = Color.GREEN;
    var status = modeler().engine().getDistributionStatus();
    var tooltip = "No k.LAB distribution is available";
    var startColor = Color.GREEN;
    var startTooltip = "Local services are not available";

    if (status.getDevelopmentStatus() == Product.Status.UP_TO_DATE
        && "source".equals(settings.getPrimaryDistribution().getValue())) {

      this.distribution = distribution;
      icon = BootstrapIcons.LAPTOP;
      tooltip = "Using locally available source k.LAB distribution";
      startTooltip = "Start local k.LAB services";

    } else {
      switch (status.getDownloadedStatus()) {
        case UNAVAILABLE -> {
          color = Color.RED;
          tooltip = "No distribution available. Click to download";
        }
        case LOCAL_ONLY -> {
          startTooltip = "Start local k.LAB services";
        }
        case UP_TO_DATE -> {
          startTooltip = "Start local k.LAB services";
          icon = BootstrapIcons.CHECK;
        }
        case OBSOLETE -> {
          color = Color.GOLDENROD;
          tooltip = "Updated k.LAB distribution available. Click to update";
          startTooltip = "Start out-of-date local services";
        }
      }
    }
    setButton(startButton, Material2MZ.POWER_SETTINGS_NEW, 32, startColor, startTooltip);
    setButton(downloadButton, icon, 24, color, tooltip);
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
