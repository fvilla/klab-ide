package org.integratedmodelling.klab.ide.components;

import java.util.*;

import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.integratedmodelling.common.logging.Logging;
import org.integratedmodelling.klab.api.authentication.CRUDOperation;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.ResourcesService;
import org.integratedmodelling.klab.api.services.RuntimeService;
import org.integratedmodelling.klab.api.services.runtime.objects.ContextInfo;
import org.integratedmodelling.klab.api.utils.Utils;
import org.integratedmodelling.klab.ide.KlabIDEController;
import org.integratedmodelling.klab.ide.Theme;
import org.integratedmodelling.klab.ide.pages.BrowsablePage;
import org.kordamp.ikonli.javafx.FontIcon;

public class DigitalTwinView extends BrowsablePage<DigitalTwinEditor> {

  //  private final Map<String, ContextScope> digitalTwins = new HashMap<>();
  private final Map<String, DigitalTwinEditor> openEditors = new HashMap<>();
  private String localServiceId;

  private List<Node> components = new ArrayList<>();
  private Node workspaceDialog;

  @Override
  public String getName() {
    return "Digital Twins";
  }

  @Override
  public Parent getView() {
    return this;
  }

  @Override
  public void reset() {}

  @Override
  protected void defineBrowser(VBox browserComponents) {

    final var digitalTwins = new ArrayList<ContextInfo>();
    KlabIDEController.modeler()
        .user()
        .getServices(RuntimeService.class)
        .forEach(
            service -> {
              var sessionInfo = service.getSessionInfo(KlabIDEController.modeler().user());
              for (var session : sessionInfo) {
                for (var contextInfo : session.getContexts()) {
                  digitalTwins.add(contextInfo);
                }
              }
            });

    digitalTwins.sort(
        (c1, c2) ->
            Utils.URLs.isLocalHost(c1.getConfiguration().getUrl())
                ? -1
                : c1.getName().compareTo(c2.getName()));

    Platform.runLater(
        () -> {
          browserComponents.getChildren().removeAll(components);
          components.clear();
          components.add(makeHeader("Digital Twins", this::addDigitalTwin));
          if (workspaceDialog != null) {
            components.add(workspaceDialog);
          }
          for (var dt : digitalTwins) {
            var dtComponent = new Components.DigitalTwin(dt, this::showDigitalTwin);
            components.add(dtComponent);
            dtComponent.createContent();
          }
          browserComponents.getChildren().addAll(components);

//          var buttonBox = new VBox(5);
//          var newButton = new Button("New Digital Twin");
//          newButton.setOnAction(e -> addDigitalTwin());
//          newButton.setMaxWidth(Double.MAX_VALUE);
//          buttonBox.getChildren().add(newButton);
//          browserComponents.getChildren().add(buttonBox);
        });
  }

  private void addDigitalTwin() {
    this.workspaceDialog = createDigitalTwinDialog();
    updateBrowser();
  }

  private Node createDigitalTwinDialog() {

    var availableServices =
        KlabIDEController.modeler().user().getServices(RuntimeService.class).stream()
            .filter(
                s ->
                    s.capabilities(KlabIDEController.modeler().user())
                        .getPermissions()
                        .contains(CRUDOperation.CREATE))
            .toList();

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setStyle("-fx-background-color: -color-neutral-muted;");
    grid.setPadding(new Insets(6, 6, 6, 6));

    TextField workspaceTitle = new TextField();
    workspaceTitle.setPromptText("DT name");
    TextArea description = new TextArea();
    description.setPromptText("Description");
    description.setPrefRowCount(3);
    final ComboBox<String> serviceSelector = new ComboBox<>();
    serviceSelector
        .getItems()
        .addAll(availableServices.stream().map(RuntimeService::getServiceName).toList());
    serviceSelector.setMaxWidth(Double.MAX_VALUE);
    var ok = new Button("Create");
    var cancel = new Button("Cancel");
    var service = (ResourcesService) null;
    ok.setOnAction(
        event -> {
          createDigitalTwin(
              workspaceTitle.getText(),
              description.getText(),
              availableServices.get(serviceSelector.getSelectionModel().getSelectedIndex()));
          workspaceDialog = null;
          updateBrowser();
        });
    ok.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.SUCCESS, Styles.SMALL);
    cancel.setOnAction(
        event -> {
          workspaceDialog = null;
          updateBrowser();
        });
    var buttons = new HBox(ok, cancel);
    buttons.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
    buttons.setSpacing(4);
    cancel.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.DANGER, Styles.SMALL);
    grid.add(new FontIcon(Theme.WORKSPACE_ICON), 0, 0);
    grid.add(workspaceTitle, 1, 0);
    grid.add(new FontIcon(Theme.EDIT_ICON), 0, 1);
    grid.add(description, 1, 1);
    GridPane.setFillWidth(serviceSelector, true);
    grid.getColumnConstraints().add(new ColumnConstraints());
    grid.getColumnConstraints()
        .add(new ColumnConstraints(200, 200, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));

    grid.add(new FontIcon(Theme.LOCAL_SERVICE_ICON), 0, 2);
    grid.add(serviceSelector, 1, 2);
    grid.add(buttons, 0, 3, 2, 1);

    CheckBox persistenceCheck = new CheckBox("Enable Persistence");
    CheckBox publicAccessCheck = new CheckBox("Public Access");

    grid.add(persistenceCheck, 0, 4, 2, 1);
    grid.add(publicAccessCheck, 0, 5, 2, 1);

    if (availableServices.isEmpty()) {
      grid.setDisable(true);
    } else {
      serviceSelector.getSelectionModel().select(0);
    }
    return grid;
  }

  private void createDigitalTwin(String text, String text1, RuntimeService runtimeService) {
    Logging.INSTANCE.info("DIO PETO");
  }

  public DigitalTwinEditor showDigitalTwin(ContextScope scope) {
    DigitalTwinEditor ret = null;
    hideBrowser();
    if (openEditors.containsKey(scope.getId())) {
      ret = openEditors.get(scope.getId());
      ret.requestFocus(); // FIXME must remember the tabs and select(tab) - in both cases
    } else {
      ret = new DigitalTwinEditor(scope, scope.getService(RuntimeService.class), this);
      openEditors.put(scope.getId(), ret);
      addEditor(ret, scope.getName(), new FontIcon(Theme.DIGITAL_TWINS_ICON));
      ret.edit(ret.getRootAsset());
    }
    return ret;
  }

  public void removeDigitalTwin(ContextScope scope) {
    // TODO
  }
}
