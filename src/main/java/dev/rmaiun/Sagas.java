package dev.rmaiun;

import dev.rmaiun.saga.Saga;
import dev.rmaiun.saga.SagaAction;
import dev.rmaiun.saga.SagaFlatMap;
import dev.rmaiun.saga.SagaSuccess;
import dev.rmaiun.support.SagaCompensation;
import java.util.concurrent.Callable;
import java.util.function.Function;
import net.jodah.failsafe.RetryPolicy;

public class Sagas {

  private Sagas() {
  }

  public static <A> SagaAction<A> action(String name, Callable<A> action) {
    return new SagaAction<>(name, action);
  }

  public static SagaCompensation compensation(String name, Runnable compensator) {
    return new SagaCompensation(name, compensator, new RetryPolicy<>().withMaxRetries(0));
  }
  public static SagaCompensation retryableCompensation(String name, Runnable compensator, RetryPolicy<Object> retryPolicy) {
    return new SagaCompensation(name, compensator, retryPolicy);
  }

  public static <A> Saga<A> success(A value) {
    return new SagaSuccess<>(value);
  }

  public static <A, B> Saga<B> flatMap(Saga<A> a, Function<A, Saga<B>> fB) {
    return new SagaFlatMap<>(a, fB);
  }
}
