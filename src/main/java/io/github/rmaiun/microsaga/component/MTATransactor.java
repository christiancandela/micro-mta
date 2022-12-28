package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.support.EvaluationResult;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public interface MTATransactor {

  <A> EvaluationResult<A> transact(String name, MTA<A> mta);
}