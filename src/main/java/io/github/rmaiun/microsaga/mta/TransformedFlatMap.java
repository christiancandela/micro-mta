package io.github.rmaiun.microsaga.mta;

import io.github.rmaiun.microsaga.func.StubInputFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface TransformedFlatMap<A, B, C> extends MTA<C> {
    Function<A, MTA<B>> getFunction();

    BiFunction<A, B, C> getTransformer();

    StubInputFunction<MTA<A>> getRootMTA();
}
