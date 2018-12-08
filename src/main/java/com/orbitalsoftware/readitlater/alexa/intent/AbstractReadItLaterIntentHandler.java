package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractReadItLaterIntentHandler implements RequestHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";

  @Getter(AccessLevel.PROTECTED)
  private final Instapaper instapaper;

  @Override
  public Optional<Response> handle(HandlerInput input) {
    try {
      ReadItLaterSession session = new ReadItLaterSession(instapaper, input.getAttributesManager());
      Optional<Response> response = handle(input, session);
      log.info("About to send response: {}", response.get());
      return response;
    } catch (Exception e) {
      // Wrap whatever exception was received in a RuntimeException and let the ExceptionHandler
      // deal with it.
      // TODO: Create simple RuntimeException-based exception model for the skill so this try/catch
      // isn't necessary.
      throw new RuntimeException(e);
    }
  }

  abstract Optional<Response> handle(HandlerInput put, ReadItLaterSession session) throws Exception;
}
