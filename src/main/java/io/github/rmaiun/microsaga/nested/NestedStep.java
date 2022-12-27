package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.Step;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class NestedStep<A> extends Nested<A> implements Step<A> {

  private final NestedAction<A> action;
  private final Compensation compensator;

  public NestedStep(NestedAction<A> action, Compensation compensator) {
    this.action = action;
    this.compensator = compensator;
  }

  @Override
  public NestedAction<A> getAction() {
    return action;
  }

  @Override
  public Compensation getCompensator() {
    return compensator;
  }
}
