package org.integratedmodelling.klab.ide;

import atlantafx.base.theme.*;
import javafx.scene.paint.Color;

public enum Theme {

  LIGHT_DEFAULT,
  DARK_DEFAULT,
  LIGHT_COOL,
  DARK_COOL,
  DARK_ALTERNATIVE;

  public static Theme CURRENT_THEME = LIGHT_DEFAULT;

  public static final Color REASONER_COLOR_MUTED = Color.web("#b3d1ff");
  public static final Color RESOURCES_COLOR_MUTED = Color.web("#c2f0c2");
  public static final Color RESOLVER_COLOR_MUTED = Color.web("#ffd9b3");
  public static final Color RUNTIME_COLOR_MUTED = Color.web("#ffb3b3");
  public static final Color REASONER_COLOR_ACTIVE = Color.web("#0052cc");
  public static final Color RESOURCES_COLOR_ACTIVE = Color.web("#29a329");
  public static final Color RESOLVER_COLOR_ACTIVE = Color.web("#cc6600");
  public static final Color RUNTIME_COLOR_ACTIVE = Color.web("#cc0000");


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
