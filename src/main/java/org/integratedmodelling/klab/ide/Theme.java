package org.integratedmodelling.klab.ide;

import atlantafx.base.theme.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.integratedmodelling.klab.api.data.RepositoryState;
import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.api.knowledge.KlabAsset;
import org.integratedmodelling.klab.api.knowledge.observation.Observation;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableFolder;
import org.integratedmodelling.klab.ide.components.Asset;
import org.integratedmodelling.klab.ide.components.IconLabel;
import org.integratedmodelling.klab.modeler.model.NavigableKimNamespace;
import org.integratedmodelling.klab.modeler.model.NavigableKimOntology;
import org.integratedmodelling.klab.modeler.model.NavigableProject;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.evaicons.Evaicons;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

public enum Theme {
  LIGHT_DEFAULT(false),
  DARK_DEFAULT(true),
  LIGHT_COOL(false),
  DARK_COOL(true),
  DARK_ALTERNATIVE(true);

  private boolean dark;

  Theme(boolean dark) {
    this.dark = dark;
  }

  public static void setLabel(Label label, RuntimeAsset asset) {

    Platform.runLater(
        () -> {
          switch (asset) {
            case Observation observation -> {
              label.setText(
                  observation.getName() == null
                      ? observation.getObservable().getName()
                      : observation.getName());
              label.setGraphic(getGraphics(asset));
              label.setTooltip(new Tooltip(observation.getObservable().getUrn()));
            }
            default -> label.setText("?????");
          }
        });
  }

  public boolean isDark() {
    return dark;
  }

  public static Theme CURRENT_THEME = LIGHT_DEFAULT;

  // color coding for services. TODO may be non-static, styled according to the current theme
  public static final Color REASONER_COLOR_MUTED = Color.web("#b3d1ff");
  public static final Color RESOURCES_COLOR_MUTED = Color.web("#c2f0c2");
  public static final Color RESOLVER_COLOR_MUTED = Color.web("#ffd9b3");
  public static final Color RUNTIME_COLOR_MUTED = Color.web("#ffb3b3");
  public static final Color REASONER_COLOR_ACTIVE = Color.web("#0052cc");
  public static final Color RESOURCES_COLOR_ACTIVE = Color.web("#29a329");
  public static final Color RESOLVER_COLOR_ACTIVE = Color.web("#cc6600");
  public static final Color RUNTIME_COLOR_ACTIVE = Color.web("#cc0000");

  // assets
  public static Ikon PROJECT_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon ONTOLOGY_ICON = Material2AL.LIGHTBULB;
  public static Ikon NAMESPACE_ICON = Material2AL.DEVELOPER_BOARD;
  public static Ikon MODEL_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon CONCEPT_DEFINITION_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon DEFINITION_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon BEHAVIOR_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon FOLDER_ICON = Material2AL.FOLDER_OPEN;
  public static Ikon TESTCASE_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon APP_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon COMPONENT_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon ACTION_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon STRATEGY_DOCUMENT_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon STRATEGY_ICON = Material2MZ.WORK_OUTLINE;
  public static Ikon WORKSPACE_ICON = Material2AL.APPS;
  public static Ikon UNKNOWN_ICON = Material2AL.BUILD_CIRCLE;
  public static Ikon KNOWLEDGE_GRAPH_ICON = BootstrapIcons.DIAGRAM_3;
  // views
  public static Ikon RESOURCES_ICON = FontAwesomeSolid.CUBES;
  public static Ikon WORKSPACES_ICON = Material2AL.APPS; // BootstrapIcons.BORDER_ALL;
  public static Ikon DIGITAL_TWINS_ICON = Material2AL.GRAPHIC_EQ;
  public static Ikon APPLICATION_VIEW_ICON = Material2AL.DIRECTIONS_RUN;
  public static Ikon WORLDVIEW_ICON = Evaicons.BULB_OUTLINE;
  public static Ikon INSPECTOR_ICON = Evaicons.EYE;

  // functionalities
  public static final Ikon ADD_ASSET_ICON = Material2AL.ADD_CIRCLE;
  public static final Ikon ADD_PROJECT_ICON = Evaicons.FOLDER_ADD_OUTLINE;
  public static final Ikon IMPORT_ASSET_ICON = Material2AL.IMPORT_EXPORT;
  public static final Ikon EDIT_ICON = Material2AL.EDIT;
  public static final Ikon COLLAPSE_ICON = Evaicons.COLLAPSE;
  public static final Ikon EXPAND_ICON = Evaicons.EXPAND;

  // services
  public static final Ikon LOCAL_SERVICE_ICON = Material2AL.DONUT_SMALL;
  public static final Ikon REMOTE_SERVICE_ICON_ONE = BootstrapIcons.CLOUDS_FILL;
  public static final Ikon REMOTE_SERVICE_ICON_MANY = BootstrapIcons.CLOUDS;

  public static Ikon getIcon(KlabAsset.KnowledgeClass knowledgeClass) {
    return switch (knowledgeClass) {
      case CONCEPT -> CONCEPT_DEFINITION_ICON;
      case OBSERVABLE -> CONCEPT_DEFINITION_ICON;
      case MODEL -> MODEL_ICON;
      case DEFINITION -> CONCEPT_DEFINITION_ICON;
      case RESOURCE -> RESOURCES_ICON;
      case NAMESPACE -> NAMESPACE_ICON;
      case BEHAVIOR -> BEHAVIOR_ICON;
      case SCRIPT -> BEHAVIOR_ICON;
      case TESTCASE -> TESTCASE_ICON;
      case APPLICATION -> APPLICATION_VIEW_ICON;
      case ONTOLOGY -> ONTOLOGY_ICON;
      case OBSERVATION_STRATEGY -> ONTOLOGY_ICON;
      case OBSERVATION_STRATEGY_DOCUMENT -> ONTOLOGY_ICON;
      case COMPONENT -> ONTOLOGY_ICON;
      case PROJECT -> PROJECT_ICON;
      case WORLDVIEW -> WORLDVIEW_ICON;
      case WORKSPACE -> WORKSPACE_ICON;
      case CONCEPT_STATEMENT -> CONCEPT_DEFINITION_ICON;
    };
  }

  public static Node getGraphics(Object asset) {

    int errorCount = 0;
    int warningCount = 0;
    int infoCount = 0;
    RepositoryState.Status repositoryStatus = null;

    if (asset instanceof Asset runtimeAsset) {
      asset = runtimeAsset.getDelegate();
    }

    if (asset instanceof NavigableAsset navigableAsset) {
      errorCount =
          navigableAsset.localMetadata().get(NavigableAsset.ERROR_NOTIFICATION_COUNT_KEY, 0);
      warningCount =
          navigableAsset.localMetadata().get(NavigableAsset.WARNING_NOTIFICATION_COUNT_KEY, 0);
      infoCount = navigableAsset.localMetadata().get(NavigableAsset.INFO_NOTIFICATION_COUNT_KEY, 0);
      repositoryStatus =
          navigableAsset
              .localMetadata()
              .get(NavigableAsset.REPOSITORY_STATUS_KEY, RepositoryState.Status.class);
    }

    var icon =
        switch (asset) {
          case NavigableProject ignored -> PROJECT_ICON;
          case NavigableKimOntology ignored -> ONTOLOGY_ICON;
          case NavigableKimNamespace ignored -> NAMESPACE_ICON;
          case NavigableFolder ignored -> FOLDER_ICON;
          // TODO all runtime asset first
          case RuntimeAsset ignored -> KNOWLEDGE_GRAPH_ICON;
          default -> UNKNOWN_ICON;
        };
    var color = CURRENT_THEME.getDefaultTextColor();
    if (errorCount > 0) {
      color = Color.RED;
    } else if (warningCount > 0) {
      color = Color.GOLDENROD;
    } else if (infoCount > 0) {
      color = Color.BLUE;
    }
    return new IconLabel(icon, 18, color);
  }

  public static <T> String getLabel(T asset) {
    if (asset instanceof NavigableAsset navigableAsset) {
      var repositoryStatus =
          navigableAsset
              .localMetadata()
              .get(NavigableAsset.REPOSITORY_STATUS_KEY, RepositoryState.Status.class);
      return repositoryStatusPrefix(repositoryStatus) + navigableAsset.getUrn();
    } else if (asset instanceof RuntimeAsset) {
      // TODO all real chances first
      if (asset instanceof Observation observation) {
        return observation.getName() == null
            ? observation.getObservable().getName()
            : observation.getName();
      }
      return "Knowledge Graph";
    }

    return "BLAAAAH";
  }

  private static String repositoryStatusPrefix(RepositoryState.Status repositoryStatus) {

    if (repositoryStatus == null) {
      return "";
    }

    return switch (repositoryStatus) {
      case UNTRACKED, ADDED -> "? ";
      case CONFLICTED -> "! ";
      case MODIFIED -> "> ";
      // TODO more
      default -> "";
    };
  }

  public String getStylesheet() {
    return switch (this) {
      case LIGHT_DEFAULT -> new PrimerLight().getUserAgentStylesheet();
      case DARK_DEFAULT -> new PrimerDark().getUserAgentStylesheet();
      case LIGHT_COOL -> new NordLight().getUserAgentStylesheet();
      case DARK_COOL -> new NordDark().getUserAgentStylesheet();
      case DARK_ALTERNATIVE -> new Dracula().getUserAgentStylesheet();
    };
  }

  public Color getDefaultTextColor() {
    return switch (this) {
      case LIGHT_DEFAULT -> Color.web("#24292FFF");
      case DARK_DEFAULT -> Color.web("#C9D1D9FF");
      case LIGHT_COOL -> Color.web("#2E3440FF");
      case DARK_COOL -> Color.web("#ECEFF4FF");
      case DARK_ALTERNATIVE -> Color.web("#F8F8F2FF");
    };
  }
}