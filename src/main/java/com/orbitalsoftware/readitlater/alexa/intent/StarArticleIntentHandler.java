package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.io.IOException;
import java.util.Optional;

public class StarArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "StarArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been starred and archived.");

  public StarArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws IOException {
    session.starCurrentArticle();
    return SUCCESS_PROMPT;
  }
}