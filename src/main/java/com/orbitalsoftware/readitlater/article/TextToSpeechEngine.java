package com.orbitalsoftware.readitlater.article;

import java.net.URL;

public interface TextToSpeechEngine {

  /**
   * Turns the provided text into speech audio and stores it via the returned {@link URL}.
   *
   * @param text the text to turn into speech.
   * @return the {@link URL} that links to the generated speech.
   */
  URL textToSpeech(String prefix, String text);
}
