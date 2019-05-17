package com.orbitalsoftware.readitlater.article.exception;

public class ReadItLaterDependencyException extends ReadItLaterException {

  public ReadItLaterDependencyException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReadItLaterDependencyException(Throwable cause) {
    super(cause);
  }
}
