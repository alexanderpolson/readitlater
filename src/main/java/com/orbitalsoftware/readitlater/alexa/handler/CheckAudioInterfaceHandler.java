package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.audioplayer.AudioPlayerState;
import java.util.Optional;

public class CheckAudioInterfaceHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    final AudioPlayerState audioPlayer =
        handlerInput.getRequestEnvelope().getContext().getAudioPlayer();
    return audioPlayer != null;
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {

    // TODO: Fill this in later.
    return Optional.empty();
  }
}
