package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.Sagas;
import io.github.rmaiun.microsaga.mta.MTA;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public abstract class Saga<A> implements MTA<A> {
  @Override
  public <B> Saga<B> map(Function<A, B> f) {
    return flatmap(a -> Sagas.success(f.apply(a)));
  }
  @Override
  public <B> Saga<B> flatmap(Function<A, MTA<B>> f) {
    return Sagas.flatMap(this, f);
  }

  public <B> Saga<B> then(MTA<B> b) {
    return Sagas.flatMap(this, a -> b);
  }

  public <B, C> Saga<C> zipWith(Function<A, MTA<B>> fB, BiFunction<A, B, C> transformer) {
    return new SagaTransformedFlatMap<>(x -> this, fB, transformer);
  }

  public <B, C> Saga<C> zipWith(MTA<B> b, Function<B, C> transformer) {
    return new SagaTransformedFlatMap<>(x -> this, a -> b, (in, out) -> transformer.apply(out));
  }

}
