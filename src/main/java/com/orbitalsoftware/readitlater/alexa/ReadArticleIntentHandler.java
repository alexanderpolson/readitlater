package com.orbitalsoftware.readitlater.alexa;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import java.io.IOException;
import java.util.Optional;

public class ReadArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String INTENT_NAME = "ReadArticleIntent";
  private static final String CONTINUE_PROMPT =
      "%s That's the end of page %d. There are %d pages left. Would you like to continue reading?";
  private static final String END_OF_ARTICLE =
      "%s That's the end of the article. Would you like archive, star, delete or skip the article?";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(intentName(INTENT_NAME));
  }

  @Override
  Optional<Response> handle(SessionManager session) throws IOException {
    String speechText = NO_ARTICLES;
    String cardTitle = DEFAULT_CARD_TITLE;

    Optional<Article> currentArticle = session.getCurrentArticle();

    if (currentArticle.isPresent()) {
      Article article = currentArticle.get();
      session.incrementArticlePage();
      String articleText = session.getArticleTextPrompt().get();
      if (article.isLastPage()) {
        speechText = String.format(END_OF_ARTICLE, articleText);
      } else {
        speechText =
            String.format(
                CONTINUE_PROMPT,
                articleText,
                article.getCurrentPage(),
                article.numPages() - article.getCurrentPage());
      }

      cardTitle = session.getNextStoryTitle().get();
    }
    ;

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
