package com.orbitalsoftware.harvest;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class DataPoint {
  private final Class<?> clazz;
  private final String operation;
  private final long duration;

  public final String key() {
    return clazz.getName() + "." + operation;
  }
}
