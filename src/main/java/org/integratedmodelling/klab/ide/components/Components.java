package org.integratedmodelling.klab.ide.components;

import javafx.scene.control.Control;

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

  private static class AbstractComponent {

    Control control;

    void post() {}
  }
}
