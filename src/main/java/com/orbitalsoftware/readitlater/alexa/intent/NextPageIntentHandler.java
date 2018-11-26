package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;

public class NextPageIntentHandler extends ReadArticleIntentHandler {

  private static final String INTENT_NAME = "NextPageIntent";

  public NextPageIntentHandler(@NonNull Instapaper instapaper) {
    super(instapaper, INTENT_NAME);
  }

  @Override
  Optional<Response> handle(@NonNull HandlerInput input, @NonNull ReadItLaterSession session)
      throws Exception {
    session.incrementArticlePage();
    return super.handle(input, session);
  }
}
