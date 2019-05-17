package com.orbitalsoftware.readitlater.alexa.article;

import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ArticleTextPaginator {

  private static final int CHARACTER_LIMIT = 1000;
  private static final Character SENTENCE_DELIMITER = '.';
  private static final Character WORD_DELIMITER = ' ';
  private static final String MID_SENTENCE_ENDING = "...";

  public static List<String> paginateText(@NonNull String fullArticleText, int characterLimit) {

    List<String> pages = new LinkedList<>();

    int beginIndex = 0;
    do {
      int endIndex = beginIndex + characterLimit;
      if (fullArticleText.length() - endIndex < characterLimit / 2) {
        endIndex = fullArticleText.length();
      } else {
        int attemptEndIndex =
            endIndex < fullArticleText.length() ? endIndex : fullArticleText.length();
        String pageAttempt = fullArticleText.substring(beginIndex, attemptEndIndex);
        endIndex = beginIndex + pageAttempt.lastIndexOf(SENTENCE_DELIMITER) + 1;
        if (endIndex == beginIndex) {
          // TODO: Come up with good strategy for dealing with articles that have long sentences.
          // backwards. Need to either move forwards or just hard cut it off.
          endIndex = beginIndex + pageAttempt.lastIndexOf(WORD_DELIMITER) + 1;
          pageAttempt = pageAttempt + MID_SENTENCE_ENDING;
        }
      }

      pages.add(fullArticleText.substring(beginIndex, endIndex));
      log.debug(
          "page: {}, beginIndex: {}, endIndex: {}, fullArticleLength: {}",
          pages.size(),
          beginIndex,
          endIndex,
          fullArticleText.length());
      beginIndex = endIndex;
    } while (beginIndex < fullArticleText.length());
    // TODO: Remove surrounding whitespace.

    return pages;
  }

  public static List<String> paginateText(@NonNull String fullArticleText) {
    return paginateText(fullArticleText, CHARACTER_LIMIT);
  }
}
