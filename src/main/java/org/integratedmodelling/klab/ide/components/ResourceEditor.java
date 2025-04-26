package org.integratedmodelling.klab.ide.components;

import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import org.integratedmodelling.klab.api.knowledge.Resource;
import org.integratedmodelling.klab.api.view.modeler.navigation.NavigableAsset;
import org.integratedmodelling.klab.ide.pages.EditorPage;

public class ResourceEditor extends EditorPage<Resource> {

    @Override
    protected TreeView<Resource> createContentTree() {
        var ret = new TreeView<Resource>();
        return ret;
    }

    @Override
    protected HBox createMenuArea() {
        var ret = new HBox();
        return ret;
    }
}
