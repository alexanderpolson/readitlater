package com.orbitalsoftware.readitlater.alexa.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.audioplayer.PlayBehavior;
import com.amazon.ask.request.Predicates;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LaunchIntentHandler implements RequestHandler {

  private static final String EXAMPLE_AUDIO =
      "https://s3.amazonaws.com/orbitalsoftware.com.pollytest/1096290739.0.mp3";

  @Override
  public boolean canHandle(HandlerInput handlerInput) {
    return handlerInput.matches(
        Predicates.intentName("LaunchIntent").or(Predicates.intentName("AMAZON.ResumeIntent")));
  }

  @Override
  public Optional<Response> handle(HandlerInput handlerInput) {
    log.info("Starting playback...");
    return handlerInput
        .getResponseBuilder()
        .withSpeech("This is a test track.")
        .addAudioPlayerPlayDirective(PlayBehavior.REPLACE_ALL, 0L, null, "TOKEN", EXAMPLE_AUDIO)
        .withShouldEndSession(true)
        .build();
  }
}
