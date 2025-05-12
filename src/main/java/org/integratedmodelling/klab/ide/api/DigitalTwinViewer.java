package org.integratedmodelling.klab.ide.api;

import org.integratedmodelling.klab.api.knowledge.observation.Observation;

public interface DigitalTwinViewer {
    void submission(Observation observation);

    void submissionAborted(Observation observation);

    void submissionFinished(Observation observation);

    void setContext(Observation observation);

    void setObserver(Observation observation);
}
