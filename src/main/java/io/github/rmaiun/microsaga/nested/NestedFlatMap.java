package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.func.StubInputFunction;
import io.github.rmaiun.microsaga.mta.FlatMap;
import io.github.rmaiun.microsaga.mta.MTA;

import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class NestedFlatMap<A, B> extends Nested<B> implements FlatMap<A, B> {

  private final StubInputFunction<MTA<A>> a;
  private final Function<A, MTA<B>> fB;

  public NestedFlatMap(MTA<A> a, Function<A, MTA<B>> fB) {
    this.a = x -> a;
    this.fB = fB;
  }

  @Override
  public StubInputFunction<MTA<A>> getA() {
    return a;
  }

  @Override
  public Function<A, MTA<B>> getfB() {
    return fB;
  }
}
