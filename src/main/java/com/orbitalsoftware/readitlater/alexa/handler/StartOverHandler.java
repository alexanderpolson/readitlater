package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.ArticleMetadataAudio;
import com.orbitalsoftware.readitlater.article.ReadItLater;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@AllArgsConstructor
@Log4j2
public class StartOverHandler extends PlaybackRequestHandler {

  private @NonNull final ReadItLater readItLater;

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(Predicates.intentName("AMAZON.StartOverIntent"));
  }

  @Override
  protected Optional<ArticleMetadataAudio> articleMetadataAudio(Session session) {
    return readItLater.startCurrentArticleOver(session);
  }
}
