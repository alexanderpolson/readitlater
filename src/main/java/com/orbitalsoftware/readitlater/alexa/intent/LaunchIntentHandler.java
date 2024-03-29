package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.harvest.annotations.Timed;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class LaunchIntentHandler extends GetNextArticleIntentHandler {

  private static final String INTENT_NAME = "LaunchIntent";
  private static final Optional<String> SUCCESS_PROMPT = Optional.of("Welcome to read it later.");

  public LaunchIntentHandler(@NonNull Instapaper instapaper) {
    super(instapaper, INTENT_NAME);
  }

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(Predicates.requestType(LaunchRequest.class));
  }

  @Timed
  @Override
  protected Optional<String> executeRequestedAction(ReadItLaterSession session) throws Exception {
    // No Op
    return SUCCESS_PROMPT;
  }
}
