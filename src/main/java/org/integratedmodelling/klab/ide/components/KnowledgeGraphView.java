package org.integratedmodelling.klab.ide.components;

import javafx.scene.layout.BorderPane;
import org.integratedmodelling.klab.api.data.RuntimeAssetGraph;
import org.integratedmodelling.klab.api.scope.ContextScope;
import org.integratedmodelling.klab.api.services.RuntimeService;

public class KnowledgeGraphView extends BorderPane {

    private final DigitalTwinEditor editor;
    private final ContextScope scope;

    public KnowledgeGraphView(ContextScope scope, DigitalTwinEditor editor) {
        this.scope = scope;
        this.editor = editor;
    }

    public void addGraph(RuntimeAssetGraph graph) {

    }
}
