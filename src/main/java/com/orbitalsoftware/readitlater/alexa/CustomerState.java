package com.orbitalsoftware.readitlater.alexa;

import com.orbitalsoftware.instapaper.Bookmark;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CustomerState {
  @Builder.Default private Optional<Bookmark> currentArticle = Optional.empty();
  @Builder.Default private final List<Integer> articlesToSkip = new LinkedList<>();

  public boolean hasArticle() {
    return currentArticle.isPresent();
  }

  public void skipCurrentArticle() {
    getCurrentArticle().ifPresent(article -> articlesToSkip.add(article.getBookmarkId()));
    System.err.printf("%s%n", currentArticle);
    currentArticle = Optional.empty();
  }

  public static CustomerState emptyState() {
    return new CustomerState(Optional.empty(), new LinkedList<Integer>());
  }
}
