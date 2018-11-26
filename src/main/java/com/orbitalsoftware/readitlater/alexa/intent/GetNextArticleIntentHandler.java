package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.ReadItLaterSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

@Slf4j
@Data
public abstract class GetNextArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";

  private static final String PROMPT_FORMAT =
      "The next story in your queue is entitled \"%s\" and their %s remaining. What would you like to do?";

  private final String intentName;

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(Predicates.intentName(getIntentName()));
  }

  @Override
  Optional<Response> handle(HandlerInput input, ReadItLaterSession session) throws Exception {
    Optional<String> executedActionPrompt = executeRequestedAction(session);
    Optional<String> nextStoryPrompt = getNextStoryPrompt(session);

    String speechText =
        nextStoryPrompt
            .map((text) -> concatenatePrompts(executedActionPrompt, Optional.of(text)))
            .orElse(concatenatePrompts(executedActionPrompt, Optional.of(NO_ARTICLES)));
    final String cardText = StringEscapeUtils.unescapeXml(speechText);
    return input
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(DEFAULT_CARD_TITLE, speechText)
        .withReprompt(cardText)
        .withShouldEndSession(!nextStoryPrompt.isPresent())
        .build();
  }

  private Optional<String> getNextStoryPrompt(ReadItLaterSession session) {
    return session
        .getCurrentArticle()
        .map(
            (article) -> {
              // TODO: This + 1 is a hack due to the relationship between p
              //  log.info("Creating prompt for article: {}", currentArticle);age number and when it
              // needs to be incremented.
              int pagesLeft = article.numPagesLeft() + 1;
              // TO handle correct grammar with respect to single page vs. multiple pages.
              String pagesLeftDescription =
                  String.format(
                      "%s %d %s",
                      pagesLeft == 1 ? "is" : "are", pagesLeft, pagesLeft == 1 ? "page" : "pages");
              String title =
                  StringEscapeUtils.unescapeXml(
                      Jsoup.parse(article.getBookmark().getTitle()).text());
              return String.format(PROMPT_FORMAT, title, pagesLeftDescription);
            });
  }

  private String concatenatePrompts(Optional<String>... prompts) {
    return Arrays.asList(prompts).stream().map(p -> p.get()).collect(Collectors.joining(" "));
  }

  /**
   * Executes specific actions for this specific intent and then returns an optional status message.
   *
   * @param session
   * @return
   * @throws IOException
   */
  protected abstract Optional<String> executeRequestedAction(ReadItLaterSession session)
      throws Exception;
}
