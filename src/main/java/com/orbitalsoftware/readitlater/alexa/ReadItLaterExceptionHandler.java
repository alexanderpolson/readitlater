package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadItLaterExceptionHandler implements ExceptionHandler {

  private static final String ERROR_MSG = "An error has occurred. Please try again later...";

  @Override
  public boolean canHandle(HandlerInput handlerInput, Throwable throwable) {
    // For now, always handle for logging.
    return true;
  }

  @Override
  public Optional<Response> handle(HandlerInput input, Throwable throwable) {
    log.error("An error has occurred and a valid response could not be returned.", throwable);
    return input.getResponseBuilder().withSpeech(ERROR_MSG).withShouldEndSession(true).build();
  }
}
