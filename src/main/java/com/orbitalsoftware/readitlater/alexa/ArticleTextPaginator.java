package com.orbitalsoftware.readitlater.alexa;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArticleTextPaginator {

  private static final int CHARACTER_LIMIT = 1000;
  private static final Character SENTENCE_DELIMITER = '.';

  public static List<String> paginateText(@NonNull String fullArticleText, int characterLimit) {
    List<String> pages = new LinkedList<>();

    if (fullArticleText.length() < characterLimit) {
      pages.add(fullArticleText);
    } else {
      int pageCount = fullArticleText.length() / characterLimit + 1;
      int idealPageLength = fullArticleText.length() / pageCount;
      int beginIndex = 0;
      int endIndex;

      while (pages.size() < pageCount) {
        if (pages.size() == pageCount - 1) {
          endIndex = fullArticleText.length();
        } else {
          int attemptEndIndex =
              beginIndex + idealPageLength < fullArticleText.length()
                  ? beginIndex + idealPageLength
                  : fullArticleText.length();
          String pageAttempt = fullArticleText.substring(beginIndex, attemptEndIndex);
          endIndex = beginIndex + pageAttempt.lastIndexOf(SENTENCE_DELIMITER) + 1;
        }
        log.debug(
            "beginIndex: {}, endIndex: {}, idealPageLength: {}, fullArticleLength: {}",
            beginIndex,
            endIndex,
            idealPageLength,
            fullArticleText.length());
        pages.add(fullArticleText.substring(beginIndex, endIndex));
        beginIndex = endIndex;
      }
      // TODO: Remove surrounding whitespace.
    }

    return pages;
  }

  public static final void main(String[] args) throws Exception {
    String article =
        "Chrome is getting a major redesign soon, and this week new changes have started to land in the Chrome's nightly \"Canary\" build. Google is launching a new version of Material Design across its products, called the \"Google Material Theme,\" and after debuting in Android P and Gmail.com, it's starting to roll out across other Google's major products. On Chrome, this means major changes to the tab and address bar. Remember, this is just a nightly build, so things could change before the stable release. But these changes line up well with previous Chrome redesign documents. The first thing you'll notice is the tab bar. Tabs now have a rectangular shape with rounded corners instead of the trapezoidal shape of the current design. Tab separation has also undergone a lot of changes. With a single tab open, you won't see a distinct tab shape at all. The current tab is always white, and in single-tab mode, the background of the tab bar is white too so everything blends together. I like the general idea here: if you aren't using multiple tabs, there's no need to show all the tab-separation cruft. With multiple tabs open, the tab background switches to a light grey and background tabs only get vertical separators rather than distinct tab shapes. Next to the tab bar is a big plus button for adding tabs, which is considerably more obvious than Chrome's current unlabeled button. I haven't gotten to try the Mac version (these are all Windows screenshots), but according to the design docs, the new tab button will be on the left side for Macs eventually. The address bar section gets some tweaking, too. The address bar is round now, just like on Android. The autocomplete drop down is now a box, instead of a bar that spans the width of the window. To the left of the address bar is a new account button for Chrome Sync, which now shows your Google profile picture instead of the name of the account. All the general New Material Design motifs are here: everything is white and round, and there's a bit more whitespace in things like the tab bar and autocomplete box. Again, everything is subject to change, but for now this is another step on the road to a stable design. Listing image by Google Chrome";
    List<String> pages = paginateText(article, 800);
    System.out.println(
        pages.stream().map(p -> String.format("\"%s\"", p)).collect(Collectors.joining(", ")));
  }
}
