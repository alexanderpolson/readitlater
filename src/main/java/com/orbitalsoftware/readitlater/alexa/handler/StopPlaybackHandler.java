package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import java.util.Optional;

public class StopPlaybackHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {

    return handlerInput.matches(Predicates.intentName("AMAZON.StopIntent"));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    return handlerInput
        .getResponseBuilder()
        .addAudioPlayerStopDirective()
        .withShouldEndSession(true)
        .build();
  }
}
