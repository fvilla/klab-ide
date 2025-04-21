package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Spacer;
import atlantafx.base.controls.Tile;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import atlantafx.base.util.BBCodeParser;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import static javafx.geometry.HPos.RIGHT;
import static javafx.scene.layout.Priority.NEVER;

/**
 * Components are widgets that can fit into the {@link NotebookView}. They have a builder that
 * facilitates their construction and posting. They may have an ID and should save/restore their
 * state.
 */
public class Components {

  enum Type {
    Distribution,
    Message,
    UserInfo,
    ServiceInfo,
    Help,
    About,
    Settings
  }

  public interface Component {

    Type getType();

    String getTitle();
  }

  public abstract static class BaseComponent extends VBox implements Component {


    String title;
    Type type;

    public BaseComponent(Type type, String title) {
      super(10);
      this.title = title;
      this.type = type;
      createContent();
    }

    protected abstract void createContent();
  }

  public static class About extends BaseComponent {

    public About() {
      super(Type.About, "About k.LAB");
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public String getTitle() {
      return title;
    }

    protected void createContent() {
      var textFlow =
          new TextFlow(
              new Text(
                  """
                              Artificial intelligence to drive knowledge integration. Our AI-powered
                              technology holds the key to a future where wide and intuitive integration
                              and use of knowledge is possible, enabling decision-making to automatically
                              benefit from the best scientific data, models, and understanding.
                              """));
      textFlow.setMinHeight(Region.USE_PREF_SIZE);
      textFlow.setMaxHeight(Region.USE_PREF_SIZE);
      VBox.setVgrow(textFlow, Priority.ALWAYS);
      textFlow.setLineSpacing(5);
      this.getChildren().add(textFlow);
    }
  }
}
