package dev.rmaiun.saga4j.saga;


import dev.rmaiun.saga4j.func.StubInputFunction;
import java.util.function.Function;

public class SagaFlatMap<A, B> extends Saga<B> {

  private final StubInputFunction<Saga<A>> a;
  private final Function<A, Saga<B>> fB;

  public SagaFlatMap(Saga<A> a, Function<A, Saga<B>> fB) {
    this.a = x -> a;
    this.fB = fB ;
  }

  public StubInputFunction<Saga<A>> getA() {
    return a;
  }

  public Function<A, Saga<B>> getfB() {
    return fB;
  }
}
