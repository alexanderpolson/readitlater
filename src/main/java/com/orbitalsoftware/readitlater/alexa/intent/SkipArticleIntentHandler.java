package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class SkipArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "SkipArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been skipped.");

  public SkipArticleIntentHandler(@NonNull Instapaper instapaper) {
    super(instapaper, INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(@NonNull ReadItLaterSession session)
      throws Exception {
    session.skipCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
