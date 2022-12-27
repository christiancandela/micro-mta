package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.util.Utils;

/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class MTARunner<A> {

  private final MTATransactor transactor;
  private String id;
  private MTA<A> mta;

  public MTARunner(MTATransactor transactor) {
    this.transactor = transactor;
    this.id = Utils.INSTANCE.defaultId();
  }

  public EvaluationResult<A> transact() {
    return transactor.transact(id, mta);
  }

  public MTARunner<A> withId(String id) {
    this.id = id;
    return this;
  }

  public String getId() {
    return id;
  }

  public void setId(String name) {
    this.id = name;
  }

  public MTA<A> getMta() {
    return mta;
  }

  public void setMta(MTA<A> mta) {
    this.mta = mta;
  }

  public MTATransactor getTransactor() {
    return transactor;
  }
}
