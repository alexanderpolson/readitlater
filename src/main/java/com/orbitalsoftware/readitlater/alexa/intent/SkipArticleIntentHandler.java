package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.util.Optional;

public class SkipArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "SkipArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been skipped.");

  public SkipArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws Exception {
    session.skipCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
