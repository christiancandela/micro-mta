package io.github.rmaiun.microsaga;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.mta.Compensation;
import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.saga.Saga;
import io.github.rmaiun.microsaga.saga.SagaAction;
import io.github.rmaiun.microsaga.saga.SagaFlatMap;
import io.github.rmaiun.microsaga.saga.SagaSuccess;
import io.github.rmaiun.microsaga.support.NoResult;
import io.github.rmaiun.microsaga.util.Utils;
import net.jodah.failsafe.RetryPolicy;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class Sagas {

  private Sagas() {
  }

  public static <A> SagaAction<A> action(String name, Callable<A> action) {
    return new SagaAction<>(name, sagaId -> action.call(), Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static <A> SagaAction<A> action(String name, CheckedFunction<String, A> action) {
    return new SagaAction<>(name, action, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static <A> SagaAction<A> retryableAction(String name, Callable<A> action, RetryPolicy<A> retryPolicy) {
    return new SagaAction<>(name, Utils.INSTANCE.callableToCheckedFunc(action), retryPolicy);
  }

  public static <A> SagaAction<A> retryableAction(String name, CheckedFunction<String, A> action, RetryPolicy<A> retryPolicy) {
    return new SagaAction<>(name, action, retryPolicy);
  }

  public static SagaAction<NoResult> voidAction(String name, Runnable action) {

    return new SagaAction<>(name, Utils.INSTANCE.runnableToCheckedFunc(action), Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static SagaAction<NoResult> voidAction(String name, Consumer<String> action) {

    return new SagaAction<>(name, Utils.INSTANCE.consumerToCheckedFunc(action), Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static <A> SagaAction<A> actionThrows(String name, Throwable exception) {
    return new SagaAction<>(name, sagaId -> {
      throw exception;
    }, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static SagaAction<NoResult> voidRetryableAction(String name, Runnable action, RetryPolicy<NoResult> retryPolicy) {
    return new SagaAction<>(name, Utils.INSTANCE.runnableToCheckedFunc(action), retryPolicy);
  }

  public static SagaAction<NoResult> voidRetryableAction(String name, Consumer<String> action, RetryPolicy<NoResult> retryPolicy) {
    return new SagaAction<>(name, Utils.INSTANCE.consumerToCheckedFunc(action), retryPolicy);
  }

  public static Compensation compensation(String name, Runnable compensator) {
    return new Compensation(name, compensator, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static Compensation compensation(String name, Consumer<String> compensator) {
    return new Compensation(name, compensator, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static Compensation retryableCompensation(String name, Runnable compensator, RetryPolicy<Object> retryPolicy) {
    return new Compensation(name, compensator, retryPolicy);
  }

  public static Compensation retryableCompensation(String name, Consumer<String> compensator, RetryPolicy<Object> retryPolicy) {
    return new Compensation(name, compensator, retryPolicy);
  }

  public static Compensation emptyCompensation(String name) {
    return new Compensation(name, () -> {
    }, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static Compensation compensationThrows(String name, Throwable exception) {
    return new Compensation(name, () -> {
      throw new RuntimeException(exception);
    }, Utils.INSTANCE.defaultNoneRetryPolicy());
  }

  public static <A> Saga<A> success(A value) {
    return new SagaSuccess<>(value);
  }

  public static <A, B> Saga<B> flatMap(MTA<A> a, Function<A, MTA<B>> fB) {
    return new SagaFlatMap<>(a, fB);
  }
}
