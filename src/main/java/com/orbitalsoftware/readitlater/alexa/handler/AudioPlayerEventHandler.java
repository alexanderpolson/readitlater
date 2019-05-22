package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.audioplayer.PlayBehavior;
import com.amazon.ask.response.ResponseBuilder;
import com.orbitalsoftware.readitlater.alexa.Session;
import com.orbitalsoftware.readitlater.article.ArticleMetadataAudio;
import com.orbitalsoftware.readitlater.article.ArticlePageAudio;
import com.orbitalsoftware.readitlater.article.ReadItLater;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class AudioPlayerEventHandler implements RequestHandler {

  private static final String EVENT_PLAYBACK_STARTED = "PlaybackStarted";
  private static final String EVENT_PLAYBACK_NEARLY_FINISHED = "PlaybackNearlyFinished";
  private static final String EVENT_PLAYBACK_FINISHED = "PlaybackFinished";
  private static final String EVENT_PLAYBACK_FAILED = "PlaybackFailed";

  private final ReadItLater readItLater;

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.getRequest().getType().startsWith("AudioPlayer.");
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    String eventName = audioPlayerEventName(handlerInput);
    log.info("Event: {}", eventName);
    final Session session = new Session(handlerInput.getAttributesManager());

    ResponseBuilder responseBuilder = handlerInput.getResponseBuilder();

    switch (eventName) {
      case EVENT_PLAYBACK_STARTED:
        // Do nothing
        break;
      case EVENT_PLAYBACK_NEARLY_FINISHED:
        final Optional<ArticlePageAudio> currentArticlePageAudio =
            session.getCurrentArticlePageAudio();
        final String expectedPreviousToken;
        if (!currentArticlePageAudio.isPresent()) {
          // Did we just read out a title?
          final Optional<ArticlePageAudio> nextArticlePageAudio =
              readItLater.getCurrentArticlePage(session);
          if (nextArticlePageAudio.isPresent()) {
            // We should have previously read the title for this article.
            log.info(
                "Almost finished reading article title: {}",
                nextArticlePageAudio.get().getPage().getMetadata().getTitle());
            expectedPreviousToken = nextArticlePageAudio.get().getPage().getMetadata().getTitle();
            final String nextPageUri = nextArticlePageAudio.get().getPageUri();
            log.info("Enqueueing next page URI: {}", nextPageUri);
            responseBuilder.addAudioPlayerPlayDirective(
                PlayBehavior.ENQUEUE, 0L, expectedPreviousToken, nextPageUri, nextPageUri);
          }
        } else {
          expectedPreviousToken = currentArticlePageAudio.get().getPageUri();
          final Optional<ArticlePageAudio> nextArticlePageAudio =
              readItLater.advanceToNextArticlePage(session);
          log.info("Almost finished reading article page: {}", expectedPreviousToken);
          if (nextArticlePageAudio.isPresent()) {
            log.info("Enqueuing article page: {}", nextArticlePageAudio.get().getPageUri());
            final String nextPageUri = nextArticlePageAudio.get().getPageUri();
            responseBuilder.addAudioPlayerPlayDirective(
                PlayBehavior.ENQUEUE, 0L, expectedPreviousToken, nextPageUri, nextPageUri);
          } else {
            // We've reached the end of the article and we should archive it.
            // TODO: How are we going to handle favorites and deletion?
            Optional<ArticleMetadataAudio> nextArticleMetadata =
                readItLater.archiveAndGetNextArticlePage(session);

            log.info(
                "Archived article: {}",
                currentArticlePageAudio.get().getPage().getMetadata().getTitle());
            if (nextArticleMetadata.isPresent()) {
              final String nextPageUri = nextArticleMetadata.get().getTitleUri();
              log.info("Queueing up title of article: {}", nextPageUri);
              responseBuilder.addAudioPlayerPlayDirective(
                  PlayBehavior.ENQUEUE, 0L, expectedPreviousToken, nextPageUri, nextPageUri);
            }
          }
        }

        break;
      case EVENT_PLAYBACK_FINISHED:
        // Do nothing
        break;
      case EVENT_PLAYBACK_FAILED:
        log.error(
            "There was an issue playing back audio: {}", handlerInput.getRequestEnvelopeJson());
        break;
    }

    return responseBuilder.build();
  }

  private String audioPlayerEventName(final HandlerInput handlerInput) {
    return handlerInput.getRequest().getType().split("\\.")[1];
  }
}
