package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.SkillStreamHandler;

// TODO: Moved to lambda package.
public class ReadItLaterStreamHandler extends SkillStreamHandler {

  public ReadItLaterStreamHandler() {
    super(ReadItLaterSkillFactory.createInstance());
  }
}
