package org.sonar.plugins.buildbreaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.sonar.api.config.Configuration;

public class TestConfiguration implements Configuration {

  private Map<String, String> properties = new HashMap<>();

  @Override
  public Optional<String> get(String s) {
    return Optional.ofNullable(properties.get(s));
  }

  @Override
  public boolean hasKey(String s) {
    return properties.containsKey(s);
  }

  @Override
  public String[] getStringArray(String s) {
    return get(s).map(v -> v.split(",")).orElse(new String[0]);
  }

  public void setProperty(String key, String value) {
    properties.put(key, value);
  }
}
