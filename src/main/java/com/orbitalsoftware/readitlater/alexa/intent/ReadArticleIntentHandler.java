package com.orbitalsoftware.readitlater.alexa.intent;

import static com.amazon.ask.request.Predicates.intentName;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.orbitalsoftware.readitlater.alexa.Article;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;

@Slf4j
public class ReadArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  private static final String SPEECH_DELIMETER = " ";
  private static final String INTENT_NAME = "ReadArticleIntent";
  private static final String ONE_PAGE = "is %d page";
  private static final String MULTIPLE_PAGES = "are %d pages";
  private static final String CONTINUE_PROMPT =
      "That's the end of page %d. There %s left. Would you like to continue reading?";
  private static final String END_OF_ARTICLE =
      "That's the end of the article. Would you like to archive, star, delete, or skip the article?";

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
  Optional<Response> handle(@NonNull HandlerInput input, @NonNull ReadItLaterSession session)
      throws Exception {
    String speechText = NO_ARTICLES;
    String repromptText = NO_ARTICLES;
    String cardTitle = DEFAULT_CARD_TITLE;

    Optional<Article> currentArticle = session.getCurrentArticle();

    if (currentArticle.isPresent()) {
      Article article = currentArticle.get();
      log.info("Article: {}", article);
      log.info(
          "Current article page: {}/{} ({} pages left)",
          article.getCurrentPage(),
          article.numPages(),
          article.numPagesLeft());
      final String articleText = session.getArticleTextPrompt().get();
      // TODO: This needs to be generalized.
      String title = session.getCurrentArticle().get().getBookmark().getTitle();
      cardTitle = StringEscapeUtils.unescapeXml(title);

      if (article.isLastPage()) {
        repromptText = END_OF_ARTICLE;
      } else {
        repromptText =
            String.format(
                CONTINUE_PROMPT,
                article.getCurrentPage(),
                pagesLeftDescription(article.numPagesLeft()));

        // TODO: The problem with this is that it updates reading progress a bit prematurely. This
        // also means the summary at the beginning is one page shy of truth.
        session.incrementArticlePage();
      }
      speechText = String.join(SPEECH_DELIMETER, articleText, repromptText);
    }
    final String cardText = StringEscapeUtils.unescapeXml(speechText);
    return input
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(cardTitle, cardText)
        .withReprompt(repromptText)
        .withShouldEndSession(!session.hasArticle())
        .build();
  }
}
