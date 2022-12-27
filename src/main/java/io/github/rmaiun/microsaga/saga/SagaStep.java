package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.Step;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaStep<A> extends Saga<A> implements Step<A> {

  private final SagaAction<A> action;
  private final Compensation compensator;

  public SagaStep(SagaAction<A> action, Compensation compensator) {
    this.action = action;
    this.compensator = compensator;
  }

  public SagaAction<A> getAction() {
    return action;
  }

  public Compensation getCompensator() {
    return compensator;
  }
}
