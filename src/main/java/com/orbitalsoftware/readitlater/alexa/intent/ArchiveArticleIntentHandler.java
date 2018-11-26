package com.orbitalsoftware.readitlater.alexa.intent;

import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;

public class ArchiveArticleIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "ArchiveArticleIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("The article has been archived.");

  public ArchiveArticleIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  protected Optional<String> executeRequestedAction(ReadItLaterSession session) throws Exception {
    session.archiveCurrentArticle();
    return SUCCESS_PROMPT;
  }
}
