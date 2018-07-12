package com.orbitalsoftware.readitlater.alexa;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;

public class ArticleTextPaginator {

  private static final int CHARACTER_LIMIT = 1000;
  private static final Character SENTENCE_DELIMITER = '.';

  public static List<String> paginateText(@NonNull String fullArticleText, int characterLimit) {
    List<String> pages = new LinkedList<>();

    if (fullArticleText.length() < characterLimit) {
      pages.add(fullArticleText);
    } else {
      int idealPageLength = fullArticleText.length() / characterLimit;
      int beginIndex = 0;
      int endIndex;

      while (beginIndex + idealPageLength < fullArticleText.length()) {
        endIndex =
            fullArticleText
                    .substring(
                        beginIndex,
                        beginIndex + characterLimit < fullArticleText.length()
                            ? beginIndex + characterLimit
                            : fullArticleText.length())
                    .lastIndexOf(SENTENCE_DELIMITER)
                + 1
                + beginIndex;
        pages.add(fullArticleText.substring(beginIndex, endIndex));
        beginIndex = endIndex;
      }

      // TODO: Remove surrounding whitespace.
      // TODO: Don't add any empty or strings with just whitespace.

      if (beginIndex != fullArticleText.length()) {
        pages.add(fullArticleText.substring(beginIndex, fullArticleText.length()));
      }
    }

    return pages;
  }

  public static final void main(String[] args) throws Exception {
    String sentence = "Blah, blah, blah. Blah, blah, blah. Blah, blah, blah. Blah, blah, blah.";
    List<String> pages = paginateText(sentence, 20);
    System.out.println(
        pages.stream().map(p -> String.format("\"%s\"", p)).collect(Collectors.joining(", ")));
  }
}
