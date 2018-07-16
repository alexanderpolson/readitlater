package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.io.IOException;
import java.util.Optional;

public class LaunchIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "LaunchIntent";
  private static final Optional<String> SUCCESS_PROMPT =
      Optional.of("Welcome to read it later. You can ask for help at any time.");

  public LaunchIntentHandler() {
    super(INTENT_NAME);
  }

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(Predicates.requestType(LaunchRequest.class));
  }

  @Override
  protected Optional<String> executeRequestedAction(SessionManager session) throws IOException {
    // No Op
    return SUCCESS_PROMPT;
  }
}