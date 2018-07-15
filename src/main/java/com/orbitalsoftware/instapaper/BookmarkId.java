package com.orbitalsoftware.instapaper;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public static List<BookmarkId> forIds(List<Integer> ids) {
    if (ids == null) {
      return new LinkedList<>();
    } else {
      return ids.stream()
          .map(id -> BookmarkId.builder().id(id).build())
          .collect(Collectors.toList());
    }
  }
}
