package io.github.rmaiun.microsaga.mta;

import io.github.rmaiun.microsaga.func.StubInputFunction;

import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface FlatMap<A, B> extends MTA<B> {
    StubInputFunction<MTA<A>> getA();

    Function<A, MTA<B>> getfB();
}
