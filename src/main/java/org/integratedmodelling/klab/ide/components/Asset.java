package org.integratedmodelling.klab.ide.components;

import org.integratedmodelling.klab.api.data.RuntimeAsset;
import org.integratedmodelling.klab.ide.Theme;

/** Painful asset wrapper, needed because SmartGraphFX isn't very flexible. */
public class Asset implements RuntimeAsset {

  RuntimeAsset delegate;

  public Asset(RuntimeAsset target) {
    this.delegate = target;
  }

  public RuntimeAsset getDelegate() {
    return delegate;
  }

  public void setDelegate(RuntimeAsset delegate) {
    this.delegate = delegate;
  }

  public long getId() {
    return delegate.getId();
  }

  public RuntimeAsset.Type classify() {
    return delegate.classify();
  }

  @Override
  public String toString() {
    return Theme.getLabel(delegate);
  }
}
