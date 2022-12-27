package io.github.rmaiun.microsaga.mta;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface MTA<A> {
    <B> MTA<B> map(Function<A, B> f);

    <B> MTA<B> flatmap(Function<A, MTA<B>> f);

    <B> MTA<B> then(MTA<B> b);

    <B, C> MTA<C> zipWith(Function<A, MTA<B>> fB, BiFunction<A, B, C> transformer);

    <B, C> MTA<C> zipWith(MTA<B> b, Function<B, C> transformer);
}
