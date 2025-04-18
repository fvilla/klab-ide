package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.Tile;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.layout.HBox;

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

  public static class About extends Card implements Component {

    public About() {

      this.setHeader(
          new Tile(
              "Delete content",
              "Are you sure to remove this content? "
                  + "You can access this file for 7 days in your trash."));
      this.setBody(new CheckBox("Do not show it anymore"));

      var confirmBtn = new Button("Confirm");
      confirmBtn.setDefaultButton(true);
      confirmBtn.setPrefWidth(150);

      var cancelBtn = new Button("Cancel");
      cancelBtn.setPrefWidth(150);

      var dialogFooter = new HBox(20, confirmBtn, cancelBtn);
      this.setFooter(dialogFooter);
    }

    @Override
    public Type getType() {
      return Type.About;
    }

    @Override
    public String getTitle() {
      return "About";
    }
  }
}
