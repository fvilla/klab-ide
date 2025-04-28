package org.integratedmodelling.klab.ide;

import atlantafx.base.theme.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableFolder;
import org.integratedmodelling.klab.modeler.model.NavigableKimNamespace;
import org.integratedmodelling.klab.modeler.model.NavigableKimOntology;
import org.integratedmodelling.klab.modeler.model.NavigableProject;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
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

  public boolean isDark() {
    return dark;
  }

  public static Theme CURRENT_THEME = DARK_COOL;

  public static final Color REASONER_COLOR_MUTED = Color.web("#b3d1ff");
  public static final Color RESOURCES_COLOR_MUTED = Color.web("#c2f0c2");
  public static final Color RESOLVER_COLOR_MUTED = Color.web("#ffd9b3");
  public static final Color RUNTIME_COLOR_MUTED = Color.web("#ffb3b3");
  public static final Color REASONER_COLOR_ACTIVE = Color.web("#0052cc");
  public static final Color RESOURCES_COLOR_ACTIVE = Color.web("#29a329");
  public static final Color RESOLVER_COLOR_ACTIVE = Color.web("#cc6600");
  public static final Color RUNTIME_COLOR_ACTIVE = Color.web("#cc0000");

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

  public static Node getGraphics(Object asset) {
    return new FontIcon(
        switch (asset) {
          case NavigableProject ignored -> PROJECT_ICON;
          case NavigableKimOntology ignored -> ONTOLOGY_ICON;
          case NavigableKimNamespace ignored -> NAMESPACE_ICON;
          case NavigableFolder ignored -> FOLDER_ICON;
          default -> UNKNOWN_ICON;
        });
  }

  public static <T> String getLabel(T asset) {
    if (asset instanceof NavigableAsset navigableAsset) {
      return navigableAsset.getUrn();
    }
    return "BLAAAAH";
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
