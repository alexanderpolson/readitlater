package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.interfaces.playbackcontroller.NextCommandIssuedRequest;
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
public class NextPlaybackHandler extends PlaybackRequestHandler {

  private @NonNull final ReadItLater readItLater;

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(
        Predicates.intentName("AMAZON.NextIntent")
            .or(Predicates.requestType(NextCommandIssuedRequest.class)));
  }

  @Override
  protected Optional<ArticleMetadataAudio> articleMetadataAudio(Session session) {
    return readItLater.skipToNextArticle(session);
  }
}
