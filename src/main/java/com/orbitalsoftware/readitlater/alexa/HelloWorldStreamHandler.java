package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.amazon.ask.model.SessionEndedRequest;

public class HelloWorldStreamHandler extends SkillStreamHandler {

    private static final String STATE_TABLE_NAME = "HelloWorldState";

    private static Skill getSkill() {
        return Skills.standard()
                .withTableName(STATE_TABLE_NAME)
                .withAutoCreateTable(true)
                .addRequestHandlers(
                        new CancelAndStopIntentHandler(),
                        new HelloWorldIntentHandler(),
                        new CountIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler())
                .build();
    }

    public HelloWorldStreamHandler() {
        super(getSkill());

    }
}
