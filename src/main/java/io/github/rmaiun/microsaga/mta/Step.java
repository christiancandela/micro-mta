package io.github.rmaiun.microsaga.mta;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface Step<A> extends MTA<A> {
    Action<A> getAction();

    Compensation getCompensator();
}
