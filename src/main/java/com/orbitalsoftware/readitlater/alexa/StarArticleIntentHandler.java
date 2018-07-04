package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.Response;

import java.io.IOException;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class StarArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "StarArticleIntent";
  private static final String STAR_MSG = "The article has been starred.";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  @Override
  Optional<Response> handle(HandlerInput input, SessionManager session) throws IOException {
    session.starCurrentArticle();
    return input
        .getResponseBuilder()
        .withSpeech(STAR_MSG)
        .addDelegateDirective(
            Intent.builder().withName(GetNextArticleRequestHandler.INTENT_NAME).build())
        .build();
  }
}
