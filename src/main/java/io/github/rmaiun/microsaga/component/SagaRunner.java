package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.util.Utils;

/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaRunner<A> {

  private final SagaTransactor sagaTransactor;
  private String id;
  private MTA<A> saga;

  public SagaRunner(SagaTransactor sagaTransactor) {
    this.sagaTransactor = sagaTransactor;
    this.id = Utils.INSTANCE.defaultId();
  }

  public EvaluationResult<A> transact() {
    return sagaTransactor.transact(id, saga);
  }

  public SagaRunner<A> withId(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public void setId(String name) {
    this.id = name;
  }

  public MTA<A> getSaga() {
    return saga;
  }

  public void setSaga(MTA<A> saga) {
    this.saga = saga;
  }

  public SagaTransactor getSagaTransactor() {
    return sagaTransactor;
  }
}
