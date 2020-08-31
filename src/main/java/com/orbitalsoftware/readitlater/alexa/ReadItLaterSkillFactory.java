package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.oauth.OAuthTokenProvider;
import com.orbitalsoftware.oauth.PropertiesOAuthTokenProvider;
import com.orbitalsoftware.readitlater.alexa.clients.InstapaperServiceWithRetryStrategies;
import com.orbitalsoftware.readitlater.alexa.handler.AudioPlayerEventHandler;
import com.orbitalsoftware.readitlater.alexa.handler.ExceptionEncounteredHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LaunchIntentRequestHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LoggingRequestHandler;
import com.orbitalsoftware.readitlater.alexa.handler.NextPlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.PausePlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.PreviousPlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.StartOverHandler;
import com.orbitalsoftware.readitlater.alexa.handler.StopPlaybackHandler;
import com.orbitalsoftware.readitlater.article.AmazonPollyTextToSpeechEngine;
import com.orbitalsoftware.readitlater.article.ReadItLater;
import com.orbitalsoftware.readitlater.article.ReadItLaterImpl;
import com.orbitalsoftware.readitlater.article.TextToSpeechEngine;
import java.io.InputStream;

public class ReadItLaterSkillFactory {

  private static final String STATE_TABLE_NAME = "ReadItLaterState";
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";

  public static Skill createInstance(final OAuthTokenProvider credentialsProvider)
      throws Exception {
    // TODO: Instantiate these via Spring.
    InputStream inputStream =
        ReadItLaterSkillFactory.class.getClassLoader().getResourceAsStream(AUTH_TOKEN_RESOURCE);
    Instapaper instapaper =
        new InstapaperServiceWithRetryStrategies(
            credentialsProvider, new PropertiesOAuthTokenProvider(inputStream));

    // Not sure if we need to specify credentials here given Lambda's execution role?
    final AmazonPolly amazonPolly = AmazonPollyClient.builder().build();
    final String amazonPollyVoiceId = "Salli";
    final AmazonS3 amazonS3 = AmazonS3ClientBuilder.defaultClient();
    final String s3Bucket = "com.orbitalsoftware.readitlater";

    TextToSpeechEngine textToSpeechEngine =
        new AmazonPollyTextToSpeechEngine(amazonPolly, amazonPollyVoiceId, amazonS3, s3Bucket);
    ReadItLater readItLater =
        ReadItLaterImpl.builder()
            .instapaper(instapaper)
            .textToSpeechEngine(textToSpeechEngine)
            .build();
    return Skills.standard()
        .withTableName(STATE_TABLE_NAME)
        .withAutoCreateTable(true)
        .addExceptionHandler(new ReadItLaterExceptionHandler())
        .addRequestHandlers(
            new LoggingRequestHandler(),
            //            new CheckAudioInterfaceHandler(),
            new LaunchIntentRequestHandler(readItLater),
            new NextPlaybackHandler(readItLater),
            new PreviousPlaybackHandler(readItLater),
            new PausePlaybackHandler(),
            new StopPlaybackHandler(),
            //            new LoopOnHandler(),
            //            new LoopOffHandler(),
            //            new ShuffleOnHandler(),
            //            new ShuffleOffHandler(),
            new StartOverHandler(readItLater),
            new AudioPlayerEventHandler(readItLater),
            new ExceptionEncounteredHandler(),
            //            new HelpIntentHandler(),
            new SessionEndedRequestHandler())
        .build();
  }
}
