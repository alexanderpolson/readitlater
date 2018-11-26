package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;

public class SkipArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "SkipArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been skipped.");

  public SkipArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(ReadItLaterSession session) throws Exception {
    session.skipCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
