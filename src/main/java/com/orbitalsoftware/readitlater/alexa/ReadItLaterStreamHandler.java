package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;

public class ReadItLaterStreamHandler extends SkillStreamHandler {

  private static final String STATE_TABLE_NAME = "ReadItLaterState";

  private static Skill getSkill() {
    return Skills.standard()
        .withTableName(STATE_TABLE_NAME)
        .withAutoCreateTable(true)
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

  public ReadItLaterStreamHandler() {
    super(getSkill());
  }
}
