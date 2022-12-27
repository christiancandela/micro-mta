package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.func.StubInputFunction;
import io.github.rmaiun.microsaga.mta.FlatMap;
import io.github.rmaiun.microsaga.mta.MTA;

import java.util.function.Function;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaFlatMap<A, B> extends Saga<B> implements FlatMap<A, B> {

  private final StubInputFunction<MTA<A>> a;
  private final Function<A, MTA<B>> fB;

  public SagaFlatMap(MTA<A> a, Function<A, MTA<B>> fB) {
    this.a = x -> a;
    this.fB = fB;
  }

  public StubInputFunction<MTA<A>> getA() {
    return a;
  }

  public Function<A, MTA<B>> getfB() {
    return fB;
  }
}
