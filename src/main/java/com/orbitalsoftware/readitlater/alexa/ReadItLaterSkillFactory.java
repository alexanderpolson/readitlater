package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.auth.PropertiesInstapaperAuthTokenProvider;
import com.orbitalsoftware.oauth.SystemPropertyOAuthCredentialsProvider;
import com.orbitalsoftware.readitlater.alexa.clients.InstapaperServiceWithRetryStrategies;
import com.orbitalsoftware.readitlater.alexa.intent.ArchiveArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.CancelAndStopIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.DeleteArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.HelpIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.LaunchIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.ReadArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.SkipArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.StarArticleIntentHandler;
import java.io.InputStream;

public class ReadItLaterSkillFactory {

  private static final String STATE_TABLE_NAME = "ReadItLaterState";
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";

  public static Skill createInstance() throws Exception {
    // TODO: Instantiate these via Spring.
    InputStream inputStream =
        PropertiesInstapaperAuthTokenProvider.class
            .getClassLoader()
            .getResourceAsStream(AUTH_TOKEN_RESOURCE);
    Instapaper instapaper =
        new InstapaperServiceWithRetryStrategies(
            new SystemPropertyOAuthCredentialsProvider(),
            new PropertiesInstapaperAuthTokenProvider(inputStream));
    return Skills.standard()
        .withTableName(STATE_TABLE_NAME)
        .withAutoCreateTable(true)
        .addExceptionHandler(new ReadItLaterExceptionHandler())
        .addRequestHandlers(
            new LaunchIntentHandler(instapaper),
            new ArchiveArticleIntentHandler(instapaper),
            new DeleteArticleIntentHandler(instapaper),
            new SkipArticleIntentHandler(instapaper),
            new StarArticleIntentHandler(instapaper),
            new ReadArticleIntentHandler(instapaper),
            new CancelAndStopIntentHandler(),
            new HelpIntentHandler(),
            new SessionEndedRequestHandler())
        .build();
  }
}
