package io.github.rmaiun.microsaga.saga;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.mta.Action;
import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.CompensationBuilder;
import io.github.rmaiun.microsaga.util.Utils;
import net.jodah.failsafe.RetryPolicy;

import java.util.function.Consumer;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class SagaAction<A> extends Saga<A> implements Action<A> {

  private final String name;
  private final RetryPolicy<A> retryPolicy;
  private final CheckedFunction<String, A> action;

  public SagaAction(String name, CheckedFunction<String, A> action) {
    this.name = name;
    this.action = action;
    this.retryPolicy = Utils.INSTANCE.defaultNoneRetryPolicy();
  }

  public SagaStep<A> compensate(Compensation compensation) {
    return new SagaStep<>(this, compensation);
  }

  public SagaStep<A> compensate(String name, Runnable compensation) {
    return new SagaStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(Utils.INSTANCE.defaultNoneRetryPolicy()).build() );
  }

  public SagaStep<A> compensate(String name, Runnable compensation, RetryPolicy<Object> retryPolicy) {
    throw new RuntimeException("Un supported operation");
  }

  public SagaStep<A> compensate(String name, Consumer<String> compensation) {
    return new SagaStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(Utils.INSTANCE.defaultNoneRetryPolicy()).build() );
  }

  public SagaStep<A> compensate(String name, Consumer<String> compensation, RetryPolicy<Object> retryPolicy) {
    throw new RuntimeException("Un supported operation");
  }

  public SagaStep<A> withoutCompensation() {
    return new SagaStep<>(this, CompensationBuilder.technical().build());
  }

  public String getName() {
    return name;
  }

  public CheckedFunction<String, A> getAction() {
    return action;
  }

  public RetryPolicy<A> getRetryPolicy() {
    return retryPolicy;
  }
}
