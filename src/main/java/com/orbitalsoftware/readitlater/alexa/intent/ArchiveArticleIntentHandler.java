package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.io.IOException;
import java.util.Optional;

public class ArchiveArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "ArchiveArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been archived.");

  public ArchiveArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws IOException {
    session.archiveCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
