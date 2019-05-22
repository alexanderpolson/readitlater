package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ExceptionEncounteredHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.getRequest().getType().equals("System.ExceptionEncountered");
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    log.error("System error occurred: {}", handlerInput.getRequestEnvelopeJson());
    return handlerInput.getResponseBuilder().withShouldEndSession(false).build();
  }
}
