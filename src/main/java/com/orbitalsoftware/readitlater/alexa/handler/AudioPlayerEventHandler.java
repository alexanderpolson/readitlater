package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AudioPlayerEventHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.getRequest().getType().startsWith("AudioPlayer.");
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    final String eventName = handlerInput.getRequest().getType().split(".")[1];
    log.info("Event: {}", eventName);
    return handlerInput.getResponseBuilder().build();
  }
}
