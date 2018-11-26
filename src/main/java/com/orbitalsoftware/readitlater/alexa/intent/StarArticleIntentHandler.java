package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class StarArticleIntentHandler extends ArchiveArticleIntentHandler {

  private static final String INTENT_NAME = "StarArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been starred and archived.");

  public StarArticleIntentHandler(@NonNull Instapaper instapaper) {
    super(instapaper, INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(@NonNull ReadItLaterSession session)
      throws Exception {
    getInstapaper()
        .starBookmark(
            StarBookmarkRequest.builder()
                .bookmarkId(session.getCurrentArticle().get().getBookmark().getBookmarkId().getId())
                .build());
    // Also archive it so we move to the next article.
    super.executeRequestedAction(session);
    return SUCCESS_PROMPT;
  }
}
