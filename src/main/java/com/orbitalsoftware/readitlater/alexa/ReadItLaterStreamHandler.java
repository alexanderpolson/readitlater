package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.SkillStreamHandler;

public class ReadItLaterStreamHandler extends SkillStreamHandler {

  public ReadItLaterStreamHandler() {
    super(ReadItLaterSkillFactory.createInstance());
  }
}
