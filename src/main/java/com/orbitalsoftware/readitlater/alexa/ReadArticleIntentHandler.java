package com.orbitalsoftware.readitlater.alexa;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.io.IOException;
import java.util.Optional;

public class ReadArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "ReadArticleIntent";
  private static final String ONE_PAGE = "is %d page";
  private static final String MULTIPLE_PAGES = "are %d pages";
  private static final String CONTINUE_PROMPT =
      "%s That's the end of page %d. There %s left. Would you like to continue reading?";
  private static final String END_OF_ARTICLE =
      "That's the end of the article. Would you like archive, star, delete or skip the article?";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  private String pagesLeftDescription(int pagesLeft) {
    if (pagesLeft == 1) {
      return String.format(ONE_PAGE, pagesLeft);
    } else {
      return String.format(MULTIPLE_PAGES, pagesLeft);
    }
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    String speechText = NO_ARTICLES;
    String cardTitle = DEFAULT_CARD_TITLE;

    Optional<Article> currentArticle = session.getCurrentArticle();

    if (currentArticle.isPresent()) {
      Article article = currentArticle.get();
      if (article.isLastPage()) {
        speechText = END_OF_ARTICLE;
      } else {
        session.incrementArticlePage();
        String articleText = session.getArticleTextPrompt().get();
        if (article.isLastPage()) {
          speechText = articleText + END_OF_ARTICLE;
        } else {
          speechText =
              String.format(
                  CONTINUE_PROMPT,
                  articleText,
                  article.getCurrentPage(),
                  pagesLeftDescription(article.numPages() - article.getCurrentPage()));
        }
      }

      cardTitle = session.getNextStoryTitle().get();
    }

    return session
        .getInput()
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(cardTitle, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!session.hasArticle())
        .build();
  }
}
