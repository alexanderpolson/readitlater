package com.orbitalsoftware.instapaper;

import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookmarkId {

  private final Integer id;
  @Builder.Default private Optional<String> hash = Optional.empty();

  public String toHaveId() {
    return getHash().map(h -> String.format("%s:%s", getId(), h)).orElse(getId().toString());
  }
}
