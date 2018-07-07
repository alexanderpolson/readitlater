package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import java.io.IOException;
import java.util.Optional;

public class GetNextArticleRequestHandler extends AbstractReadItLaterIntentHandler {
  static final String INTENT_NAME = "GetNextArticleIntent";
  private static final String NEXT_ARTICLE_FORMAT = "%s";
  private static final String LAUNCH_TEXT_FORMAT =
      "Welcome to read it later. You can ask for help at any time. " + NEXT_ARTICLE_FORMAT;

  private boolean isLaunchRequest(HandlerInput input) {
    return input.matches(Predicates.requestType(LaunchRequest.class));
  }

  private boolean isGetNextArticleRequest(HandlerInput input) {
    return input.matches(Predicates.intentName(INTENT_NAME));
  }

  public boolean canHandle(HandlerInput input) {
    return isLaunchRequest(input) || isGetNextArticleRequest(input);
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    Optional<String> nextStoryPrompt = session.getNextStoryPrompt();
    String promptFormat =
        isLaunchRequest(session.getInput()) ? LAUNCH_TEXT_FORMAT : NEXT_ARTICLE_FORMAT;
    String speechText =
        nextStoryPrompt
            .map((text) -> String.format(promptFormat, text))
            .orElse(String.format(promptFormat, NO_ARTICLES));
    return session
        .getInput()
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(DEFAULT_CARD_TITLE, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!nextStoryPrompt.isPresent())
        .build();
  }
}
