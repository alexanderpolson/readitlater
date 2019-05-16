package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.auth.PropertiesInstapaperAuthTokenProvider;
import com.orbitalsoftware.oauth.OAuthCredentialsProvider;
import com.orbitalsoftware.readitlater.alexa.clients.InstapaperServiceWithRetryStrategies;
import com.orbitalsoftware.readitlater.alexa.handler.AudioPlayerEventHandler;
import com.orbitalsoftware.readitlater.alexa.handler.CheckAudioInterfaceHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LaunchIntentHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LoggingRequestHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LoopOffHandler;
import com.orbitalsoftware.readitlater.alexa.handler.LoopOnHandler;
import com.orbitalsoftware.readitlater.alexa.handler.NextPlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.PausePlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.PreviousPlaybackHandler;
import com.orbitalsoftware.readitlater.alexa.handler.ShuffleOffHandler;
import com.orbitalsoftware.readitlater.alexa.handler.ShuffleOnHandler;
import com.orbitalsoftware.readitlater.alexa.handler.StartOverHandler;
import com.orbitalsoftware.readitlater.alexa.intent.HelpIntentHandler;
import java.io.InputStream;

public class ReadItLaterSkillFactory {

  private static final String STATE_TABLE_NAME = "ReadItLaterState";
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";

  public static Skill createInstance(final OAuthCredentialsProvider credentialsProvider)
      throws Exception {
    // TODO: Instantiate these via Spring.
    InputStream inputStream =
        PropertiesInstapaperAuthTokenProvider.class
            .getClassLoader()
            .getResourceAsStream(AUTH_TOKEN_RESOURCE);
    Instapaper instapaper =
        new InstapaperServiceWithRetryStrategies(
            credentialsProvider, new PropertiesInstapaperAuthTokenProvider(inputStream));
    return Skills.standard()
        .withTableName(STATE_TABLE_NAME)
        .withAutoCreateTable(true)
        .addExceptionHandler(new ReadItLaterExceptionHandler())
        .addRequestHandlers(
            new LoggingRequestHandler(),
            new LaunchIntentHandler(),
            new CheckAudioInterfaceHandler(),
            new NextPlaybackHandler(),
            new PreviousPlaybackHandler(),
            new PausePlaybackHandler(),
            new LoopOnHandler(),
            new LoopOffHandler(),
            new ShuffleOnHandler(),
            new ShuffleOffHandler(),
            new StartOverHandler(),
            new AudioPlayerEventHandler(),
            new HelpIntentHandler(),
            new SessionEndedRequestHandler())
        .build();

    //    new LaunchIntentHandler(instapaper),
    //            new ArchiveArticleIntentHandler(instapaper),
    //            new DeleteArticleIntentHandler(instapaper),
    //            new SkipArticleIntentHandler(instapaper),
    //            new StarArticleIntentHandler(instapaper),
    //            new NextPageIntentHandler(instapaper),
    //            new ReadArticleIntentHandler(instapaper),
    //            new CancelAndStopIntentHandler(),
    //            new HelpIntentHandler(),
    //            new SessionEndedRequestHandler())
    //        .build();
  }
}
