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

  private final StubInputFunction<MTA<A>> rootMTA;
  private final Function<A, MTA<B>> function;
  private final BiFunction<A, B, C> transformer;

  public NestedTransformedFlatMap(StubInputFunction<MTA<A>> mta, Function<A, MTA<B>> function, BiFunction<A, B, C> transformer) {
    this.rootMTA = mta;
    this.function = function;
    this.transformer = transformer;
  }

  @Override
  public Function<A, MTA<B>> getFunction() {
    return function;
  }

  @Override
  public BiFunction<A, B, C> getTransformer() {
    return transformer;
  }

  @Override
  public StubInputFunction<MTA<A>> getRootMTA() {
    return rootMTA;
  }
}
