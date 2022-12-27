package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.func.StubInputFunction;
import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.mta.TransformedFlatMap;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class NestedTransformedFlatMap<A, B, C> extends Nested<C> implements TransformedFlatMap<A, B, C> {

  private final StubInputFunction<MTA<A>> rootSaga;
  private final Function<A, MTA<B>> sagaFunc;
  private final BiFunction<A, B, C> transformer;

  public NestedTransformedFlatMap(StubInputFunction<MTA<A>> saga, Function<A, MTA<B>> sagaFunc, BiFunction<A, B, C> transformer) {
    this.rootSaga = saga;
    this.sagaFunc =sagaFunc;
    this.transformer = transformer;
  }

  @Override
  public Function<A, MTA<B>> getSagaFunc() {
    return sagaFunc;
  }

  @Override
  public BiFunction<A, B, C> getTransformer() {
    return transformer;
  }

  @Override
  public StubInputFunction<MTA<A>> getRootSaga() {
    return rootSaga;
  }
}
