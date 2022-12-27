package io.github.rmaiun.microsaga;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.CompensationBuilder;
import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.nested.Nested;
import io.github.rmaiun.microsaga.nested.NestedAction;
import io.github.rmaiun.microsaga.nested.NestedFlatMap;
import io.github.rmaiun.microsaga.nested.NestedSuccess;
import io.github.rmaiun.microsaga.support.NoResult;
import io.github.rmaiun.microsaga.util.Utils;
import net.jodah.failsafe.RetryPolicy;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public class Nesteds {

  private Nesteds() {
  }

  public static <A> NestedAction<A> action(String name, Callable<A> action) {
    return new NestedAction<>(name, sagaId -> action.call(), Utils.INSTANCE.defaultRetryPolicy());
  }

  public static <A> NestedAction<A> action(String name, CheckedFunction<String, A> action) {
    return new NestedAction<>(name, action, Utils.INSTANCE.defaultRetryPolicy());
  }

  public static <A> NestedAction<A> retryableAction(String name, Callable<A> action, RetryPolicy<A> retryPolicy) {
    return new NestedAction<>(name, Utils.INSTANCE.callableToCheckedFunc(action), retryPolicy);
  }

  public static <A> NestedAction<A> retryableAction(String name, CheckedFunction<String, A> action, RetryPolicy<A> retryPolicy) {
    return new NestedAction<>(name, action, retryPolicy);
  }

  public static NestedAction<NoResult> voidAction(String name, Runnable action) {

    return new NestedAction<>(name, Utils.INSTANCE.runnableToCheckedFunc(action), Utils.INSTANCE.defaultRetryPolicy());
  }

  public static NestedAction<NoResult> voidAction(String name, Consumer<String> action) {

    return new NestedAction<>(name, Utils.INSTANCE.consumerToCheckedFunc(action), Utils.INSTANCE.defaultRetryPolicy());
  }

  public static <A> NestedAction<A> actionThrows(String name, Throwable exception) {
    return new NestedAction<>(name, sagaId -> {
      throw exception;
    }, Utils.INSTANCE.defaultRetryPolicy());
  }

  public static NestedAction<NoResult> voidRetryableAction(String name, Runnable action, RetryPolicy<NoResult> retryPolicy) {
    return new NestedAction<>(name, Utils.INSTANCE.runnableToCheckedFunc(action), retryPolicy);
  }

  public static NestedAction<NoResult> voidRetryableAction(String name, Consumer<String> action, RetryPolicy<NoResult> retryPolicy) {
    return new NestedAction<>(name, Utils.INSTANCE.consumerToCheckedFunc(action), retryPolicy);
  }

  public static Compensation compensation(String name, Runnable compensator) {
    return CompensationBuilder.create().name(name).compensation(compensator).retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build();
  }

  public static Compensation compensation(String name, Consumer<String> compensator) {
    return CompensationBuilder.create().name(name).compensation(compensator).retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build();
  }

  public static Compensation retryableCompensation(String name, Runnable compensator, RetryPolicy<Object> retryPolicy) {
    return CompensationBuilder.create().name(name).compensation(compensator).retryPolicy(retryPolicy).build();
  }

  public static Compensation retryableCompensation(String name, Consumer<String> compensator, RetryPolicy<Object> retryPolicy) {
    return CompensationBuilder.create().name(name).compensation(compensator).retryPolicy(retryPolicy).build();
  }

  public static Compensation emptyCompensation(String name) {
    return CompensationBuilder.create().name(name).compensation(() -> {}).retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build();
  }

  public static Compensation compensationThrows(String name, Throwable exception) {
    return CompensationBuilder.create().name(name).compensation(() -> {
      throw new RuntimeException(exception);
    }).retryPolicy(Utils.INSTANCE.defaultRetryPolicy()).build();
  }

  public static <A> Nested<A> success(A value) {
    return new NestedSuccess<>(value);
  }

  public static <A, B> Nested<B> flatMap(MTA<A> a, Function<A, MTA<B>> fB) {
    return new NestedFlatMap<>(a, fB);
  }
}
