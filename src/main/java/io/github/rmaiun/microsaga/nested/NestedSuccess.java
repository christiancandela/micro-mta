package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.mta.Success;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class NestedSuccess<T> extends Nested<T> implements Success<T> {

  private final T value;

  public NestedSuccess(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }
}
