package com.orbitalsoftware.readitlater.alexa;

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
  private final List<String> pages;
  private int currentPage;

  @JsonIgnore
  public boolean isLastPage() {
    return currentPage == pages.size();
  }

  @JsonIgnore
  public int numPages() {
    return pages.size();
  }

  public void incrementCurrentPage() {
    if (isLastPage()) {
      throw new IllegalStateException("Tried to increment article past the last page.");
    }
    currentPage++;
  }

  private int getCurrentPageIndex() {
    int currentPageIndex = currentPage - 1;
    if (currentPageIndex < 0) {
      currentPageIndex = 0;
    }
    return currentPageIndex;
  }

  @JsonIgnore
  public String getCurrentPageText() {
    return pages.get(getCurrentPageIndex());
  }
}
