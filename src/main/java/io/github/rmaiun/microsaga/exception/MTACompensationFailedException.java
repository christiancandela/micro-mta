package io.github.rmaiun.microsaga.exception;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class MTACompensationFailedException extends RuntimeException {

  public MTACompensationFailedException(String action, String saga, Throwable cause) {
    super(String.format("Compensation for saga %s is failed while compensates %s action", saga, action), cause);
  }
}
