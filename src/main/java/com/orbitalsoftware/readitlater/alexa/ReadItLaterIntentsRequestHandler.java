package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import java.io.IOException;
import java.util.Optional;

public class ReadItLaterIntentsRequestHandler extends AbstractReadItLaterIntentHandler {
  private static final String GET_NEXT_ARTICLE_INTENT = "GetNextArticleIntent";
  private static final String ARCHIVE_ARTICLE_INTENT = "ArchiveArticleIntent";
  private static final String DELETE_ARTICLE_INTENT = "DeleteArticleIntent";
  private static final String STAR_ARTICLE_INTENT = "StarArticleIntent";
  private static final String SKIP_ARTICLE_INTENT = "SkipArticleIntent";

  private static final String NEXT_ARTICLE_FORMAT = "%s";
  private static final String LAUNCH_TEXT_FORMAT =
      "Welcome to read it later. You can ask for help at any time. " + NEXT_ARTICLE_FORMAT;
  private static final String ARCHIVED_FORMAT =
      "The article has been archived. " + NEXT_ARTICLE_FORMAT;
  private static final String STARRED_FORMAT =
      "The article has been starred. " + NEXT_ARTICLE_FORMAT;
  private static final String DELETED_FORMAT =
      "The article has been deleted. " + NEXT_ARTICLE_FORMAT;
  private static final String SKIPPED_FORMAT =
      "The article has been skipped. " + NEXT_ARTICLE_FORMAT;

  private boolean isLaunchRequest(HandlerInput input) {
    return input.matches(Predicates.requestType(LaunchRequest.class));
  }

  public boolean canHandle(HandlerInput input) {
    return isLaunchRequest(input) || isCoveredIntent(input);
  }

  private Optional<String> getIntentName(HandlerInput input) {
    String intentName = null;
    if (input.matches(Predicates.intentName(GET_NEXT_ARTICLE_INTENT))) {
      intentName = GET_NEXT_ARTICLE_INTENT;
    } else if (input.matches(Predicates.intentName(ARCHIVE_ARTICLE_INTENT))) {
      intentName = ARCHIVE_ARTICLE_INTENT;
    } else if (input.matches(Predicates.intentName(DELETE_ARTICLE_INTENT))) {
      intentName = DELETE_ARTICLE_INTENT;
    } else if (input.matches(Predicates.intentName(STAR_ARTICLE_INTENT))) {
      intentName = STAR_ARTICLE_INTENT;
    } else if (input.matches(Predicates.intentName(SKIP_ARTICLE_INTENT))) {
      intentName = SKIP_ARTICLE_INTENT;
    }

    return Optional.ofNullable(intentName);
  }

  private boolean isCoveredIntent(HandlerInput input) {
    return getIntentName(input).isPresent();
  }

  private String executeRequestedAction(SessionManager session) throws IOException {
    String promptFormat = NEXT_ARTICLE_FORMAT;
    if (isLaunchRequest(session.getInput())) {
      promptFormat = LAUNCH_TEXT_FORMAT;
    } else {
      switch (getIntentName(session.getInput()).get()) {
        case ARCHIVE_ARTICLE_INTENT:
          session.archiveCurrentArticle();
          promptFormat = ARCHIVED_FORMAT;
          break;
        case DELETE_ARTICLE_INTENT:
          session.deleteCurrentArticle();
          promptFormat = DELETED_FORMAT;
          break;
        case STAR_ARTICLE_INTENT:
          session.starCurrentArticle();
          promptFormat = STARRED_FORMAT;
          break;
        case SKIP_ARTICLE_INTENT:
          session.skipCurrentArticle();
          promptFormat = SKIPPED_FORMAT;
          break;
      }
    }

    return promptFormat;
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    final String promptFormat = executeRequestedAction(session);
    Optional<String> nextStoryPrompt = session.getNextStoryPrompt();

    String speechText =
        nextStoryPrompt
            .map((text) -> String.format(promptFormat, text))
            .orElse(String.format(promptFormat, NO_ARTICLES));
    return session
        .getInput()
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(DEFAULT_CARD_TITLE, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!nextStoryPrompt.isPresent())
        .build();
  }
}
