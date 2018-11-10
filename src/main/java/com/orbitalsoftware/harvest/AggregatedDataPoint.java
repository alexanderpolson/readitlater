package com.orbitalsoftware.harvest;

import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AggregatedDataPoint {

  @Getter private final Class<?> clazz;
  @Getter private final String operation;
  private final List<Long> durations = new LinkedList<Long>();

  @Getter private long min;
  @Getter private long max;
  private double average;
  @Getter private Long sum = Long.valueOf(0);

  public int getCount() {
    return durations.size();
  }

  public void aggregate(long duration) {
    if (duration < min || durations.isEmpty()) {
      min = duration;
    }
    if (duration > max || durations.isEmpty()) {
      max = duration;
    }
    sum += duration;
    durations.add(duration);
  }

  public double getAvg() {
    return sum.doubleValue() / durations.size();
  }
}
