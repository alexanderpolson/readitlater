package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.audioplayer.PlayBehavior;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.ArticleMetadataAudio;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@AllArgsConstructor
@Log4j2
public abstract class PlaybackRequestHandler implements RequestHandler {

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    final Session session = new Session(handlerInput.getAttributesManager());
    final Optional<ArticleMetadataAudio> articleMetadataAudio = articleMetadataAudio(session);

    if (articleMetadataAudio.isPresent()) {
      return handleStartNextArticle(handlerInput, articleMetadataAudio.get());
    } else {
      return handleNoArticlesAvailable(handlerInput);
    }
  }

  protected abstract Optional<ArticleMetadataAudio> articleMetadataAudio(Session session);

  private Optional<Response> handleStartNextArticle(
      @NonNull final HandlerInput handlerInput,
      @NonNull final ArticleMetadataAudio articleMetadataAudio) {
    log.info("Starting playback...");
    return handlerInput
        .getResponseBuilder()
        .addAudioPlayerPlayDirective(
            PlayBehavior.REPLACE_ALL,
            0L,
            null,
            articleMetadataAudio.getTitleUri(),
            articleMetadataAudio.getTitleUri())
        .withShouldEndSession(true)
        .build();
  }

  private Optional<Response> handleNoArticlesAvailable(@NonNull final HandlerInput handlerInput) {
    log.info("No article found.");
    return handlerInput
        .getResponseBuilder()
        .withSpeech("No articles are currently available. Queue up some articles and try again.")
        .withShouldEndSession(true)
        .build();
  }
}
