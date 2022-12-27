package io.github.rmaiun.microsaga.mta;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import net.jodah.failsafe.RetryPolicy;

import java.util.function.Consumer;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface Action<A> extends MTA<A> {
    Step<A> compensate(Compensation compensation);

    Step<A> compensate(String name, Runnable compensation);

    Step<A> compensate(String name, Runnable compensation, RetryPolicy<Object> retryPolicy);

    Step<A> compensate(String name, Consumer<String> compensation);

    Step<A> compensate(String name, Consumer<String> compensation, RetryPolicy<Object> retryPolicy);

    Step<A> withoutCompensation();

    String getName();

    CheckedFunction<String, A> getAction();

    RetryPolicy<A> getRetryPolicy();
}
