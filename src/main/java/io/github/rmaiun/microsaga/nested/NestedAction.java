package io.github.rmaiun.microsaga.nested;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.mta.Action;
import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.CompensationBuilder;
import io.github.rmaiun.microsaga.util.Utils;
import net.jodah.failsafe.RetryPolicy;

import java.util.function.Consumer;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class NestedAction<A> extends Nested<A> implements Action<A> {

  private final String name;
  private final RetryPolicy<A> retryPolicy;
  private final CheckedFunction<String, A> action;

  public NestedAction(String name, CheckedFunction<String, A> action, RetryPolicy<A> retryPolicy) {
    this.name = name;
    this.action = action;
    this.retryPolicy = retryPolicy;
  }

  @Override
  public NestedStep<A> compensate(Compensation compensation) {
    return new NestedStep<>(this, compensation);
  }

  @Override
  public NestedStep<A> compensate(String name, Runnable compensation) {
    return new NestedStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build() );
  }

  @Override
  public NestedStep<A> compensate(String name, Runnable compensation, RetryPolicy<Object> retryPolicy) {
    return new NestedStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(retryPolicy).build() );
  }

  @Override
  public NestedStep<A> compensate(String name, Consumer<String> compensation) {
    return new NestedStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build() );
  }

  @Override
  public NestedStep<A> compensate(String name, Consumer<String> compensation, RetryPolicy<Object> retryPolicy) {
    return new NestedStep<>(this, CompensationBuilder.create().name(name).compensation(compensation)
            .retryPolicy(retryPolicy).build() );
  }

  @Override
  public NestedStep<A> withoutCompensation() {
    return new NestedStep<>(this, CompensationBuilder.technical()
            .retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build());
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public CheckedFunction<String, A> getAction() {
    return action;
  }

  @Override
  public RetryPolicy<A> getRetryPolicy() {
    return retryPolicy;
  }
}
