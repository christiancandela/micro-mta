package io.github.rmaiun.microsaga.mta;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public interface Success<T> extends MTA<T> {
    T getValue();
}
