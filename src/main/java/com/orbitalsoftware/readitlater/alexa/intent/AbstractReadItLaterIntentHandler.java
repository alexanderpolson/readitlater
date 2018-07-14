package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.io.IOException;
import java.util.Optional;

public abstract class AbstractReadItLaterIntentHandler implements RequestHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";

  @Override
  public Optional<Response> handle(HandlerInput input) {
    try {
      SessionManager session = new SessionManager(input);
      Optional<Response> response = handle(session);
      System.err.printf("About to send response: %s%n", response.get());
      return response;
    } catch (Exception e) {
      // Wrap whatever exception was received in a RuntimeException and let the ExceptionHandler
      // deal with it.
      // TODO: Create simple RuntimeException-based exception model for the skill so this try/catch
      // isn't necessary.
      throw new RuntimeException(e);
    }
  }

  abstract Optional<Response> handle(SessionManager session) throws IOException;
}
