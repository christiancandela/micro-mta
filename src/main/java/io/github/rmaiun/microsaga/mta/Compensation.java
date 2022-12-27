package io.github.rmaiun.microsaga.mta;

import net.jodah.failsafe.RetryPolicy;

import java.util.function.Consumer;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class Compensation {
  private final String name;
  private final Consumer<String> compensation;
  private final RetryPolicy<Object> retryPolicy;
  private final boolean technical;

  public Compensation(String name, Runnable compensation) {
    this(name,sagaId -> compensation.run(),new RetryPolicy<>().withMaxRetries(0),false);
  }
  public Compensation(String name, Runnable compensation, RetryPolicy<Object> retryPolicy) {
    this(name,sagaId -> compensation.run(),retryPolicy,false);
  }
  public Compensation(String name, Consumer<String> compensation, RetryPolicy<Object> retryPolicy) {
    this(name,compensation,retryPolicy,false);
  }
  public Compensation(String name, Runnable compensation, RetryPolicy<Object> retryPolicy, boolean technical) {
    this(name,sagaId -> compensation.run(),retryPolicy,technical);
  }
  public Compensation(String name, Consumer<String> compensation, RetryPolicy<Object> retryPolicy, boolean technical) {
    this.name = name;
    this.compensation = compensation;
    this.retryPolicy = retryPolicy;
    this.technical = technical;
  }
  public String getName() {
    return name;
  }

  public Consumer<String> getCompensation() {
    return compensation;
  }

  public RetryPolicy<Object> getRetryPolicy() {
    return retryPolicy;
  }

  public boolean isTechnical() {
    return technical;
  }

}
