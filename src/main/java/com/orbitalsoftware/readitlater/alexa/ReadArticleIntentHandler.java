package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;

import java.io.IOException;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class ReadArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "ReadArticleIntent";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  @Override
  Optional<Response> handle(HandlerInput input, SessionManager session) throws IOException {
    String speechText = session.getArticleTextPrompt().orElse(NO_ARTICLES);
    String cardTitle = session.getNextStoryTitle().orElse(DEFAULT_CARD_TITLE);
    return input
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(cardTitle, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!session.hasArticle())
        .build();
  }
}
