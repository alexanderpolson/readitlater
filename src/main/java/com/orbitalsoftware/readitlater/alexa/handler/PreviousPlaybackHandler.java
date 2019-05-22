package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.interfaces.playbackcontroller.PreviousCommandIssuedRequest;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.ArticleMetadataAudio;
import com.orbitalsoftware.readitlater.article.ReadItLater;
import java.util.Optional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PreviousPlaybackHandler extends PlaybackRequestHandler {

  private final ReadItLater readItLater;

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(
        Predicates.intentName("AMAZON.PreviousIntent")
            .or(Predicates.requestType(PreviousCommandIssuedRequest.class)));
  }

  @Override
  protected Optional<ArticleMetadataAudio> articleMetadataAudio(Session session) {
    return readItLater.previousArticle(session);
  }
}
