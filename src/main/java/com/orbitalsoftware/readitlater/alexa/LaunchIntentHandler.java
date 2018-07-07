package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import java.util.Optional;

public class LaunchIntentHandler implements RequestHandler {

  private static final String LAUNCH_TEXT =
      "Welcome to read it later. You can ask for help at any time.";

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(Predicates.requestType(LaunchRequest.class));
  }

  @Override
  public Optional<Response> handle(HandlerInput input) {
    return input
        .getResponseBuilder()
        .withSpeech(LAUNCH_TEXT)
        .withSimpleCard("Read it later", LAUNCH_TEXT)
        //        .addDelegateDirective(
        //            Intent.builder().withName(GetNextArticleRequestHandler.INTENT_NAME).build())
        .withShouldEndSession(false)
        .build();
  }
}
