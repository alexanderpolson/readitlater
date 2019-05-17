package com.orbitalsoftware.readitlater.article.exception;

public class ReadItLaterIllegalArgumentException extends ReadItLaterException {

  public ReadItLaterIllegalArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadItLaterIllegalArgumentException(Throwable cause) {
    super(cause);
  }
}
