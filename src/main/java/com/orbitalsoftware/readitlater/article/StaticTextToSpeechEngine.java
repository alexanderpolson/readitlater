package com.orbitalsoftware.readitlater.article;

import java.net.MalformedURLException;
import java.net.URL;

public class StaticTextToSpeechEngine implements TextToSpeechEngine {

  private static final String STATIC_SPEECH_URL =
      "https://s3.amazonaws.com/orbitalsoftware.com.pollytest/test_static_speech.mp3";

  @Override
  public URL textToSpeech(String text) {
    try {
      return new URL(STATIC_SPEECH_URL);
    } catch (MalformedURLException e) {
      // Execution should never reach this point.
      return null;
    }
  }
}
