package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class DeleteArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "DeleteArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been deleted.");

  public DeleteArticleIntentHandler(@NonNull Instapaper instapaper) {
    super(instapaper, INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(@NonNull ReadItLaterSession session)
      throws Exception {
    getInstapaper()
        .deleteBookmark(
            DeleteBookmarkRequest.builder()
                .bookmarkId(session.getCurrentArticle().get().getBookmark().getBookmarkId().getId())
                .build());
    return SUCCESS_PROMPT;
  }
}
