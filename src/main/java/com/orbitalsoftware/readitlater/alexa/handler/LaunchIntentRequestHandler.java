package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.ArticleMetadataAudio;
import com.orbitalsoftware.readitlater.article.ReadItLater;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class LaunchIntentRequestHandler extends PlaybackRequestHandler {

  private @NonNull final ReadItLater readItLater;

  @Override
  public boolean canHandle(HandlerInput handlerInput) {

    boolean canHandle =
        handlerInput.matches(
            Predicates.requestType(LaunchRequest.class)
                .or(Predicates.intentName("AMAZON.ResumeIntent")));
    return canHandle;
  }

  @Override
  protected Optional<ArticleMetadataAudio> articleMetadataAudio(Session session) {
    session.clearCurrentArticlePage();
    return readItLater.getCurrentArticleTitle(session);
  }
}
