package io.github.rmaiun.microsaga.exception;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class MTAActionFailedException extends RuntimeException {

  public MTAActionFailedException(String action, String saga, Throwable cause) {
    super(String.format("Action %s for saga %s is failed", action, saga), cause);
  }
}
