package com.orbitalsoftware.readitlater.alexa.intent;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.orbitalsoftware.readitlater.alexa.SessionManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public abstract class GetNextArticleIntentHandler extends AbstractReadItLaterIntentHandler {

  protected static final String DEFAULT_CARD_TITLE = "Read It Later";
  protected static final String NO_ARTICLES =
      "You don't appear to have any articles. Come back once you've added some.";

  private final String intentName;

  @Override
  public boolean canHandle(HandlerInput input) {
    return input.matches(Predicates.intentName(getIntentName()));
  }

  @Override
  Optional<Response> handle(SessionManager session) throws Exception {
    Optional<String> executedActionPrompt = executeRequestedAction(session);
    Optional<String> nextStoryPrompt = session.getNextStoryPrompt();

    String speechText =
        nextStoryPrompt
            .map((text) -> concatenatePrompts(executedActionPrompt, Optional.of(text)))
            .orElse(concatenatePrompts(executedActionPrompt, Optional.of(NO_ARTICLES)));
    return session
        .getInput()
        .getResponseBuilder()
        .withSpeech(speechText)
        .withSimpleCard(DEFAULT_CARD_TITLE, speechText)
        .withReprompt(speechText)
        .withShouldEndSession(!nextStoryPrompt.isPresent())
        .build();
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
  protected abstract Optional<String> executeRequestedAction(SessionManager session)
      throws Exception;
}
