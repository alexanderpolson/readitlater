package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.auth.PropertiesInstapaperAuthTokenProvider;
import com.orbitalsoftware.oauth.SystemPropertyOAuthCredentialsProvider;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import com.orbitalsoftware.readitlater.alexa.clients.InstapaperServiceWithRetryStrategies;
import java.io.InputStream;
import java.util.Optional;

public abstract class AbstractReadItLaterIntentHandler implements RequestHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";

  @Override
  public Optional<Response> handle(HandlerInput input) {
    try {
      // TODO: Instantiate these via Spring.
      InputStream inputStream =
          PropertiesInstapaperAuthTokenProvider.class
              .getClassLoader()
              .getResourceAsStream(AUTH_TOKEN_RESOURCE);
      InstapaperService instapaper =
          new InstapaperServiceWithRetryStrategies(
              new SystemPropertyOAuthCredentialsProvider(),
              new PropertiesInstapaperAuthTokenProvider(inputStream));
      ReadItLaterSession session = new ReadItLaterSession(instapaper, input.getAttributesManager());
      Optional<Response> response = handle(input, session);
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

  abstract Optional<Response> handle(HandlerInput put, ReadItLaterSession session) throws Exception;
}
