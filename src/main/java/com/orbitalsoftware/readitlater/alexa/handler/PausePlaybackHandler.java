package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.playbackcontroller.PauseCommandIssuedRequest;
import com.amazon.ask.request.Predicates;
import java.util.Optional;

public class PausePlaybackHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {

    return handlerInput.matches(
        Predicates.intentName("AMAZON.PauseIntent")
            .or(Predicates.requestType(PauseCommandIssuedRequest.class)));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    return handlerInput.getResponseBuilder().addAudioPlayerStopDirective().build();
  }
}
