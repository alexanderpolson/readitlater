package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.util.Optional;

public class DeleteArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "DeleteArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been deleted.");

  public DeleteArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws Exception {
    session.deleteCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
