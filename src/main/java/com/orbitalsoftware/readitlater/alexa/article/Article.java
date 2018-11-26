package com.orbitalsoftware.readitlater.alexa.article;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.orbitalsoftware.instapaper.Bookmark;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Article {

  private static final int FIRST_PAGE = 1;

  private final Bookmark bookmark;

  // TODO: Come up with a better name for this.
  private final int pageOffset;
  private final List<String> pages;
  private final List<Integer> pageLengths;
  private int currentPage;

  @JsonIgnore
  public boolean isLastPage() {
    return currentPage == pageLengths.size();
  }

  @JsonIgnore
  public int numPages() {
    return pageLengths.size();
  }

  private int pageIndexForNumber(int pageNumber) {
    if (pageNumber < 1) {
      throw new IllegalArgumentException("Page number must be 1 or greater.");
    }
    if (pageNumber > numPages()) {
      throw new IllegalArgumentException(
          String.format("Page number cannot be greater than %d.", numPages()));
    }
    return pageNumber - pageOffset - 1;
  }

  @JsonIgnore
  public boolean hasCurrentPage() {
    return pageIndexForNumber(currentPage) < pages.size();
  }

  @JsonIgnore
  public boolean isMissingCurrentPage() {
    return !hasCurrentPage();
  }

  @JsonIgnore
  public int numPagesLeft() {
    return numPages() - getCurrentPage();
  }

  private double articleCharacterLength() {
    return pageLengths.stream().mapToInt((p) -> p).sum();
  }

  public double progressPercentage() {
    double charactersRead = 0;
    for (int i = 0; i < currentPage; i++) {
      charactersRead += pageLengths.get(i);
    }

    return charactersRead / articleCharacterLength();
  }

  public void incrementCurrentPage() {
    if (isLastPage()) {
      throw new IllegalStateException("Tried to increment article past the last page.");
    }
    currentPage++;
  }

  @JsonIgnore
  public int getCurrentPageIndex() {
    return currentPage - 1 - pageOffset;
  }

  @JsonIgnore
  public String getCurrentPageText() {
    return pages.get(getCurrentPageIndex());
  }
}
