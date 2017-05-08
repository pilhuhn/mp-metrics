package de.bsd.mp_metrics.impl;

import de.bsd.mp_metrics.Metadata;

/**
 * @author hrupp
 */
public class ConfigHolder {
  private static final ConfigHolder ourInstance = new ConfigHolder();
  private Metadata config;

  public static ConfigHolder getInstance() {
    return ourInstance;
  }

  private ConfigHolder() {
  }

  public void setConfig(Metadata config) {
    this.config = config;
  }

  public Metadata getConfig() {
    synchronized (ourInstance) {
      if (config == null) {
        config = new Metadata();
      }
    }
    return config;
  }
}
