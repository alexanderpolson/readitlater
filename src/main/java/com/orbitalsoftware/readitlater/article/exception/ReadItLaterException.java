package com.orbitalsoftware.readitlater.article.exception;

public abstract class ReadItLaterException extends RuntimeException {

  public ReadItLaterException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadItLaterException(Throwable cause) {
    super(cause);
  }
}
