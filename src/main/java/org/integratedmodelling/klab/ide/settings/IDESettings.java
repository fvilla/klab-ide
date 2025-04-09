package org.integratedmodelling.klab.ide.settings;

import org.integratedmodelling.common.configuration.Settings;
import org.integratedmodelling.klab.api.configuration.Configuration;

public class IDESettings extends Settings {

    public static final String PRIMARY_DISTRIBUTION = "klab.modeler.distribution.primary";

    private Setting<String> primaryDistribution =
      new Setting<>(PRIMARY_DISTRIBUTION, "source");

    public IDESettings() {
    super(Configuration.INSTANCE.getFile("modeler.toml"));
    }

    public Setting<String> getPrimaryDistribution() {
        return primaryDistribution;
    }

    public void setPrimaryDistribution(Setting<String> primaryDistribution) {
        this.primaryDistribution = primaryDistribution;
    }
}
