package org.integratedmodelling.klab.ide.components;

import atlantafx.base.controls.Card;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.integratedmodelling.klab.api.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.api.services.resources.ResourceInfo;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material2.Material2AL;
import org.kordamp.ikonli.material2.Material2MZ;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Components are widgets that can fit into the {@link NotebookView}. They have a builder that
 * facilitates their construction and posting. They may have an ID and should save/restore their
 * state.
 */
public class Components {

  public enum Type {
    Distribution,
    Message,
    UserInfo,
    ServiceInfo,
    Help,
    About,
    Settings,
    Object // these are not indexed and may be used outside the notebook
  }

  public interface Component {

    Type getType();

    String getTitle();
  }

  public abstract static class BaseComponent extends VBox implements Component {

    String title;
    Type type;

    public BaseComponent(Type type, String title, boolean initialize) {
      super(10);
      this.title = title;
      this.type = type;
      if (initialize) {
        createContent();
      }
    }

    @Override
    public Type getType() {
      return type;
    }

    @Override
    public String getTitle() {
      return title;
    }

    protected abstract void createContent();
  }

  public static class About extends BaseComponent {

    public About() {
      super(Type.About, "About k.LAB", true);
    }

    protected void createContent() {
      var card = new Card();
      try (var lg =
          this.getClass()
              .getResourceAsStream("/org/integratedmodelling/klab/ide/icons/klab-im.png")) {
        var logo = new Image(lg, 420, 180, true, true);
        HBox hBox = new HBox(new ImageView(logo));
        card.setBody(hBox);
        this.getChildren().add(card);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static class User extends BaseComponent {

    public User() {
      super(Type.UserInfo, "User information", true);
    }

    protected void createContent() {

      Label certContentLabel = new Label("k.LAB user:");
      Label certUsername = new Label("No certificate");
      Label certDescription = new Label("Drop a certificate file");
      Hyperlink hubLink = new Hyperlink("k.LAB user hub");

      VBox certificateArea = new VBox(certContentLabel, certUsername, certDescription, hubLink);

      GridPane groupIcons = new GridPane();
      Label groupsLabel = new Label("Groups");
      VBox groupArea = new VBox(groupIcons, groupsLabel);

      HBox main = new HBox(certificateArea, groupArea);
      var card = new Card();
      card.setBody(main);

      this.getChildren().add(card);
    }

    //    public void setupAuthenticationUI() {
    //      if (this.authentication != null) {
    //        switch(this.authentication.getStatus()) {
    //          case ANONYMOUS:
    //            certContentLabel.setText("No certificate");
    //            certContentLabel.setTextFill(Paint.valueOf(COLOR_RED));
    //            certUsername.setText("Anonymous");
    //            certUsername.setTextFill(Paint.valueOf(COLOR_LIGHT_GREY));
    //            certDescription.setText("Drop a certificate file here");
    //            break;
    //          case EXPIRED:
    //            certContentLabel.setText("Certificate expired!");
    //            certContentLabel.setTextFill(Paint.valueOf(COLOR_RED));
    //            certUsername.setText(this.authentication.getUsername());
    //            certUsername.setTextFill(Paint.valueOf(COLOR_RED));
    //            certDescription.setText("Expired " + this.authentication.getExpiration());
    //            break;
    //          case INVALID:
    //            certContentLabel.setText("Invalid certificate!");
    //            certContentLabel.setTextFill(Paint.valueOf(COLOR_RED));
    //            certUsername.setText(this.authentication.getUsername());
    //            certUsername.setTextFill(Paint.valueOf(COLOR_RED));
    //            certDescription.setText("Drop a valid certificate here");
    //          case OFFLINE:
    //            certContentLabel.setText("System is offline");
    //            certContentLabel.setTextFill(Paint.valueOf(COLOR_RED));
    //            certUsername.setText(this.authentication.getUsername());
    //            certUsername.setTextFill(Paint.valueOf(COLOR_LIGHT_GREY));
    //            certDescription.setText("Check network connection");
    //            break;
    //          case VALID:
    //            certContentLabel.setText("Certificate is valid");
    //            certContentLabel.setTextFill(Paint.valueOf("#666666"));
    //            certUsername.setText(this.authentication.getUsername());
    //            certUsername.setTextFill(Paint.valueOf(COLOR_GREEN));
    //            certDescription.setText("Expires " +
    // this.authentication.getExpiration().toString(DateTimeFormat.mediumDate()));
    //            break;
    //          default:
    //            break;
    //        }
    //
    //        int i = 0;
    //        List<Group> groups = this.authentication.getGroups();
    //        this.groupIconArea.getChildren().clear();
    //        for(Group group : groups) {
    //          if (i < 9) {
    //            int columnIndex = i % 3;
    //            int rowIndex = i / 3;
    //            Node groupIcon;
    //            Tooltip tooltip = new Tooltip();
    //            if (i == 8 && groups.size() > 9) {
    //              groupIcon = new Label("...");
    //              groupIcon.getStyleClass().add("group-icon");
    //              StringBuffer otherGroups = new StringBuffer();
    //              for(; i < groups.size(); i++) {
    //                otherGroups.append(groups.get(i).getId()).append("\n");
    //              }
    //              tooltip.setText(otherGroups.toString());
    //            } else {
    //              if (group.getIconUrl() != null && !"".equals(group.getIconUrl())) {
    //                Image groupImage = new Image(group.getIconUrl(), 24, 24, false, false);
    //                groupIcon = new ImageView(groupImage);
    //                groupIcon.setPickOnBounds(true);
    //              } else {
    //                StringBuffer lText = new
    // StringBuffer().append(String.valueOf(group.getId().charAt(0)).toUpperCase());
    //                if (group.getId().length() > 1) {
    //                  lText.append(String.valueOf(group.getId().charAt(1)).toUpperCase());
    //                }
    //                groupIcon = new Label(lText.toString());
    //                groupIcon.getStyleClass().add("group-icon");
    //                if (group.getId().equals(Authentication.DEVELOPER_GROUP)) {
    //                  groupIcon.getStyleClass().add("group-icon-developer");
    //                }
    //                // ((Label)groupIcon).setAlignment(Pos.CENTER);
    //              }
    //              tooltip = new Tooltip(group.getId());
    //            }
    //            this.groupIconArea.add(groupIcon, columnIndex, rowIndex);
    //            tooltip.setStyle("-fx-font-size: 12");
    //            Tooltip.install(groupIcon, tooltip);
    //            i++;
    //          } else {
    //            break;
    //          }
    //        }
    //        if (!authentication.getMessages().isEmpty()) {
    //          StringBuffer errors = new StringBuffer();
    //          StringBuffer warnings = new StringBuffer();
    //          StringBuffer infos = new StringBuffer();
    //          authentication.getMessages().forEach(m -> {
    //            StringBuffer buffer;
    //            if (m.getType() == HubNotificationMessage.Type.ERROR) {
    //              buffer = errors;
    //            } else if (m.getType() == HubNotificationMessage.Type.WARNING) {
    //              buffer = warnings;
    //            } else {
    //              buffer = infos;
    //            }
    //            switch(m.getMessageClass()) {
    //              case EXPIRED_GROUP:
    //              case EXPIRING_GROUP:
    //              case EXPIRING_CERTIFICATE:
    //                if (m.getInfo() != null) {
    //                  String sDate = (String) (Arrays.asList(m.getInfo()).stream()
    //                                                 .filter(c ->
    // c.getFirst().equals(HubNotificationMessage.ExtendedInfo.EXPIRATION_DATE))
    //                                                 .findFirst().get()).getSecond();
    //                  DateTime date = DateTime.parse(sDate);
    //                  if (m.getMessageClass() ==
    // HubNotificationMessage.MessageClass.EXPIRING_CERTIFICATE) {
    //                    buffer.append("Certificate ");
    //                  } else {
    //                    String group = (String) (Arrays.asList(m.getInfo()).stream()
    //                                                   .filter(c ->
    // c.getFirst().equals(HubNotificationMessage.ExtendedInfo.GROUP_NAME))
    //                                                   .findFirst().get()).getSecond();
    //                    buffer.append("Subscription to group ").append(group);
    //                  }
    //                  if
    // (m.getMessageClass().equals(HubNotificationMessage.MessageClass.EXPIRED_GROUP)) {
    //                    buffer.append(" has expired");
    //                  } else {
    //                    buffer.append(" will expire on
    // ").append(DateTimeFormat.forPattern("dd/MM/yyyy").print(date));
    //                  }
    //                } else {
    //                  buffer.append(m.getMsg());
    //                }
    //                break;
    //              default:
    //                buffer.append(m.getMsg());
    //                break;
    //            }
    //            buffer.append("\n");
    //          });
    //          if (errors.length() > 0)
    //            showExpirationAlert(Alert.AlertType.ERROR, errors.toString(), true);
    //          if (warnings.length() > 0)
    //            showExpirationAlert(Alert.AlertType.WARNING, warnings.toString(), true);
    //          if (infos.length() > 0)
    //            showExpirationAlert(Alert.AlertType.INFORMATION, infos.toString(), false);
    //        }
    //        // activate branch changer button
    //        if (this.authentication.isDeveloper()) {
    //          this.switchProjectsBranch.setVisible(true);
    //          this.switchProjectsBranch.setDisable(!this.hasRepositories());
    //          this.ccSwitchProjectsBranchTooltip.setText("Switch projects to "
    //                                                             +
    // (DEVELOP_BRANCH.equals(this.currentBranch) ? MASTER_BRANCH : DEVELOP_BRANCH) + " branch");
    //        } else {
    //          this.switchProjectsBranch.setVisible(false);
    //        }
    //
    //        this.checkForUpdates(true);
    //      }
    //
    //    }
  }

  public static class Distribution extends BaseComponent {

    public Distribution() {
      super(Type.Distribution, "Distribution status", true);
    }

    protected void createContent() {
      var card = new Card();
      this.getChildren().add(card);
    }
  }

  public static class Settings extends BaseComponent {

    public Settings() {
      super(Type.Settings, "Settings", true);
    }

    protected void createContent() {
      var card = new Card();
      this.getChildren().add(card);
    }
  }

  public static class Resource extends BaseComponent {

    private final Consumer<ResourceInfo> clickHandler;
    private ResourceInfo descriptor;

    public Resource(ResourceInfo descriptor, Consumer<ResourceInfo> clickHandler) {
      super(Type.Object, descriptor.getUrn(), false);
      this.descriptor = descriptor;
      this.clickHandler = clickHandler;
      createContent();
    }

    protected void createContent() {
      var card = new Card();
      VBox body = new VBox();
      var icon =
          switch (this.descriptor.getKnowledgeClass()) {
            case WORKSPACE -> new FontIcon(Material2AL.APPS);
            default -> throw new KlabInternalErrorException("DIO COP");
          };
      HBox header =
          new HBox(icon, new Label(this.descriptor.getMetadata().get("name", "Unnamed workspace")));
      HBox footer = new HBox();
      footer.setAlignment(Pos.CENTER_RIGHT);
      card.setOnMouseClicked(
          event -> {
            clickHandler.accept(this.descriptor);
          });
      card.setBody(body);
      card.setHeader(header);
      // footer has delete icon if info allow it
      card.setFooter(footer);
      this.getChildren().add(card);
    }
  }

  public static class Services extends BaseComponent {

    public Services() {
      super(Type.ServiceInfo, "Services", true);
    }

    protected void createContent() {
      var card = new Card();
      Tab reasonerTab = new Tab();
      reasonerTab.setText("Reasoner");
      Tab resourcesTab = new Tab();
      resourcesTab.setText("Resources");
      Tab resolverTab = new Tab();
      resolverTab.setText("Resolver");
      Tab runtimeTab = new Tab();
      runtimeTab.setText("Runtime");
      TabPane tabs = new TabPane(reasonerTab, resourcesTab, resolverTab, runtimeTab);
      tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
      card.setBody(tabs);
      this.getChildren().add(card);
    }
  }
}
