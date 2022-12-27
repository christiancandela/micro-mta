package io.github.rmaiun.microsaga.support;

import io.github.rmaiun.microsaga.mta.MTA;
import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class FunctionContext {

  private final Function<Object, MTA<Object>> sagaFunction;
  private final BiFunction<Object, Object, Object> transformer;

  public FunctionContext(Function<Object, MTA<Object>> sagaFunction) {
    this.sagaFunction = sagaFunction;
    this.transformer = null;
  }

  public FunctionContext(Function<Object, MTA<Object>> sagaFunction, BiFunction<Object, Object, Object> transformer) {
    this.sagaFunction = sagaFunction;
    this.transformer = transformer;
  }

  public Function<Object, MTA<Object>> getSagaFunction() {
    return sagaFunction;
  }

  public BiFunction<Object, Object, Object> getTransformer() {
    return transformer;
  }
}
