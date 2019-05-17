package com.orbitalsoftware.readitlater.article.exception;

public class ReadItLaterFatalException extends ReadItLaterException {

  public ReadItLaterFatalException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadItLaterFatalException(Throwable cause) {
    super(cause);
  }
}
