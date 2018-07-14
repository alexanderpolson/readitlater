package com.orbitalsoftware.instapaper;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateReadProgressRequest {
  private final Integer bookmarkId;
  private final Double progress;
}
