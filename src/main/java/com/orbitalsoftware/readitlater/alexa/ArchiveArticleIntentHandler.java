package com.orbitalsoftware.readitlater.alexa;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.io.IOException;
import java.util.Optional;

public class ArchiveArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "ArchiveArticleIntent";
  private static final String ARCHIVE_MSG = "The article has been archived.";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    session.archiveCurrentArticle();
    return new GetNextArticleRequestHandler().handle(session);
  }
}
