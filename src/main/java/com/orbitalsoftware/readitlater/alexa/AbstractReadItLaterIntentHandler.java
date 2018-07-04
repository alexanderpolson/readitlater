package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.io.IOException;
import java.util.Optional;

public abstract class AbstractReadItLaterIntentHandler implements RequestHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";

  private static final String ERROR_MSG = "An error has occurred. Please try again later...";

  @Override
  public Optional<Response> handle(HandlerInput input) {
    try {
      SessionManager session = new SessionManager(input.getRequestEnvelope().getSession());
      return handle(input, session);
    } catch (Exception e) {
      return input.getResponseBuilder().withSpeech(ERROR_MSG).withShouldEndSession(true).build();
    }
  }

  abstract Optional<Response> handle(HandlerInput input, SessionManager session) throws IOException;
}
