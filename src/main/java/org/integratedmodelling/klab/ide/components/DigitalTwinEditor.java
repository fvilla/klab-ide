package org.integratedmodelling.klab.ide.components;

import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.ide.pages.EditorPage;

public class DigitalTwinEditor extends EditorPage<ContextScope> {

    @Override
    protected void onSingleClickItemSelection(ContextScope value) {

    }

    @Override
    protected void onDoubleClickItemSelection(ContextScope value) {

    }

    @Override
    protected TreeView<ContextScope> createContentTree() {
        return null;
    }

    @Override
    protected HBox createMenuArea() {
        return null;
    }
}
