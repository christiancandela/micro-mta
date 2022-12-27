package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.Nesteds;
import io.github.rmaiun.microsaga.mta.MTA;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public abstract class Nested<A> implements MTA<A> {

  @Override
  public <B> Nested<B> map(Function<A, B> f) {
    return flatmap(a -> Nesteds.success(f.apply(a)));
  }

  @Override
  public <B> Nested<B> flatmap(Function<A, MTA<B>> f) {
    return Nesteds.flatMap(this, f);
  }

  @Override
  public <B> Nested<B> then(MTA<B> b) {
    return Nesteds.flatMap(this, a -> b);
  }

  @Override
  public <B, C> Nested<C> zipWith(Function<A, MTA<B>> fB, BiFunction<A, B, C> transformer) {
    return new NestedTransformedFlatMap<>(x ->this, fB, transformer);
  }

  @Override
  public <B, C> Nested<C> zipWith(MTA<B> b, Function<B, C> transformer) {
    return new NestedTransformedFlatMap<>(x -> this, a -> b, (in, out) -> transformer.apply(out));
  }
}
