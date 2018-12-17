package com.orbitalsoftware.readitlater.alexa.intent;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.harvest.annotations.Timed;
import java.util.Optional;

// TODO: Come back to this and make it more intelligent.
public class CancelAndStopIntentHandler implements RequestHandler {
  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
  }

  @Timed
  @Override
  public Optional<Response> handle(HandlerInput input) {
    return input.getResponseBuilder().withSpeech("Thanks for using Read It Later").build();
  }
}
