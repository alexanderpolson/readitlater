package com.orbitalsoftware.readitlater.alexa;

import java.io.IOException;
import java.util.Optional;

public class SkipArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "SkiprticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been skipped.");

  public SkipArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws IOException {
    session.skipCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
