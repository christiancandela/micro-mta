package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.mta.Success;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaSuccess<T> extends Saga<T> implements Success<T> {

  private final T value;

  public SagaSuccess(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }
}
