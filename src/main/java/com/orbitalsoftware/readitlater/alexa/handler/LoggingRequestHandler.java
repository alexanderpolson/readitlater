package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.Response;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggingRequestHandler implements RequestHandler {

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    Request request = handlerInput.getRequest();
    log.info("Received request: {}", request.getType());
    return false;
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    return Optional.empty();
  }
}
