package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class ArchiveArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "ArchiveArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been archived.");

  public ArchiveArticleIntentHandler(@NonNull Instapaper instapaper) {
    this(instapaper, INTENT_NAME);
  }

  /**
   * This is only intended to be called by intents that want to take some action before or after
   * archiving the currrent story (like the star intent). There may be a better way to do this
   * that's more elegant.
   *
   * @param instapaper
   * @param intentName
   */
  protected ArchiveArticleIntentHandler(
      @NonNull Instapaper instapaper, @NonNull String intentName) {
    super(instapaper, intentName);
  }

  @Override
  protected Optional<String> executeRequestedAction(@NonNull ReadItLaterSession session)
      throws Exception {
    getInstapaper()
        .archiveBookmark(
            ArchiveBookmarkRequest.builder()
                .bookmarkId(session.getCurrentArticle().get().getBookmark().getBookmarkId().getId())
                .build());
    return SUCCESS_PROMPT;
  }
}
