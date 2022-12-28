package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.func.StubInputFunction;
import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.mta.TransformedFlatMap;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaTransformedFlatMap<A, B, C> extends Saga<C> implements TransformedFlatMap<A, B, C> {

  private final StubInputFunction<MTA<A>> rootSaga;
  private final Function<A, MTA<B>> sagaFunc;
  private final BiFunction<A, B, C> transformer;

  public SagaTransformedFlatMap(StubInputFunction<MTA<A>> saga, Function<A, MTA<B>> sagaFunc, BiFunction<A, B, C> transformer) {
    this.rootSaga = saga;
    this.sagaFunc = sagaFunc;
    this.transformer = transformer;
  }

  public Function<A, MTA<B>> getFunction() {
    return sagaFunc;
  }

  public BiFunction<A, B, C> getTransformer() {
    return transformer;
  }

  public StubInputFunction<MTA<A>> getRootMTA() {
    return rootSaga;
  }
}
