package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.mta.MTA;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class MTAManager {

  private final MTATransactor transactor;

  public MTAManager() {
    this.transactor = new DefaultMTATransactor();
  }

  public MTAManager(MTATransactor transactor) {
    this.transactor = transactor;
  }

  public static <A> MTARunner<A> use(MTA<A> saga, MTATransactor transactor) {
    MTARunner<A> runner = new MTARunner<>(transactor);
    runner.setMta(saga);
    return runner;
  }

  public static <A> MTARunner<A> use(MTA<A> mta) {
    MTARunner<A> runner = new MTARunner<>(new DefaultMTATransactor());
    runner.setMta(mta);
    return runner;
  }

  public <A> MTARunner<A> saga(MTA<A> mta) {
    MTARunner<A> ctx = new MTARunner<>(transactor);
    ctx.setMta(mta);
    return ctx;
  }
}
