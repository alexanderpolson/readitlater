package com.orbitalsoftware.readitlater.alexa;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.io.IOException;
import java.util.Optional;

public class ReadArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "ReadArticleIntent";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    String speechText = session.getArticleTextPrompt().orElse(NO_ARTICLES);
    String cardTitle = session.getNextStoryTitle().orElse(DEFAULT_CARD_TITLE);
    return session
        .getInput()
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(cardTitle, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!session.hasArticle())
        .build();
  }
}
