package com.orbitalsoftware.harvest;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import lombok.NonNull;

public class ExecutionTimer implements Closeable {

  private static final String METRIC_MSG_FORMAT = "%s.%s took %d msec\n";
  private static final String SUMMARY_LINE_FORMAT =
      "Operation %s.%s - n: %d, avg: %f, min: %d, max: %d\n";
  private static final Map<String, AggregatedDataPoint> allDataPoints = new TreeMap<>();

  private final Class<?> clazz;
  private final String operation;
  private final long startTime;

  public ExecutionTimer(@NonNull final Class<?> clazz, @NonNull final String operation) {
    this.clazz = clazz;
    this.operation = operation;
    this.startTime = System.currentTimeMillis();
  }

  private static void enqueueDataPoint(@NonNull final DataPoint dataPoint) {
    // Not thread safe
    String key = dataPoint.key();
    AggregatedDataPoint agg =
        allDataPoints.getOrDefault(
            key, new AggregatedDataPoint(dataPoint.getClazz(), dataPoint.getOperation()));
    allDataPoints.put(key, agg);
    agg.aggregate(dataPoint.getDuration());
  }

  public static void summarize() {
    allDataPoints.forEach(
        (key, dataPoint) -> {
          System.err.printf(
              SUMMARY_LINE_FORMAT,
              dataPoint.getClazz().getName(),
              dataPoint.getOperation(),
              dataPoint.getCount(),
              dataPoint.getAvg(),
              dataPoint.getMin(),
              dataPoint.getMax());
        });
  }

  @Override
  public void close() throws IOException {
    long duration = System.currentTimeMillis() - startTime;
    System.err.printf(METRIC_MSG_FORMAT, clazz.getName(), operation, duration);
    enqueueDataPoint(new DataPoint(clazz, operation, duration));
  }
}
