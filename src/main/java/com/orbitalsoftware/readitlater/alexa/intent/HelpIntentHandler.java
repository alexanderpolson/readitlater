package com.orbitalsoftware.readitlater.alexa.intent;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import java.util.Optional;

public class HelpIntentHandler implements RequestHandler {
  private static final String helpText =
      "You can say \"Read article\", \"Skip article\", \"Star article\", \"Archive article\", or \"Delete article\".";

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(intentName("AMAZON.HelpIntent"));
  }

  @Override
  public Optional<Response> handle(HandlerInput input) {
    return input
        .getResponseBuilder()
        .withSpeech(helpText)
        .withSimpleCard("HelloWorld", helpText)
        .withReprompt(helpText)
        .withShouldEndSession(false)
        .build();
  }
}
