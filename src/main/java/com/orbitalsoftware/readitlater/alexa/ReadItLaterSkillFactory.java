package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.orbitalsoftware.readitlater.alexa.intent.ArchiveArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.CancelAndStopIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.DeleteArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.HelpIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.LaunchIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.ReadArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.SkipArticleIntentHandler;
import com.orbitalsoftware.readitlater.alexa.intent.StarArticleIntentHandler;

public class ReadItLaterSkillFactory {

  private static final String STATE_TABLE_NAME = "ReadItLaterState";

  public static Skill createInstance() {
    return Skills.standard()
        .withTableName(STATE_TABLE_NAME)
        .withAutoCreateTable(true)
        .addExceptionHandler(new ReadItLaterExceptionHandler())
        .addRequestHandlers(
            new LaunchIntentHandler(),
            new ArchiveArticleIntentHandler(),
            new DeleteArticleIntentHandler(),
            new SkipArticleIntentHandler(),
            new StarArticleIntentHandler(),
            new ReadArticleIntentHandler(),
            new CancelAndStopIntentHandler(),
            new HelpIntentHandler(),
            new SessionEndedRequestHandler())
        .build();
  }
}
