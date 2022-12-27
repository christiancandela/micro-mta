package io.github.rmaiun.microsaga.util;

import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.support.NoResult;
import net.jodah.failsafe.RetryPolicy;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
/**
 * Creado por Christian Candela, basado en el trabajo de Roman Maiun
 *
 */
public enum Utils {
  INSTANCE;

  private static final int DEFAULT_RETRIES = 3;

  Utils() {
  }

  public String defaultId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public <A> RetryPolicy<A> defaultNoneRetryPolicy() {
    return new RetryPolicy<A>().withMaxRetries(0);
  }

  public <A> RetryPolicy<A> defaultRetryPolicy() {
    return new RetryPolicy<A>().withMaxRetries(DEFAULT_RETRIES);
  }
  public <A> CheckedFunction<String, A> funcAsChecked(Function<String, A> callable) {
    return callable::apply;
  }

  public Callable<NoResult> voidRunnableToCallable(Runnable r) {
    return () -> {
      r.run();
      return NoResult.instance();
    };
  }

  public <A> CheckedFunction<String, A> callableToCheckedFunc(Callable<A> c) {
    return sagaId -> c.call();
  }

  public CheckedFunction<String, NoResult> consumerToCheckedFunc(Consumer<String> c) {
    return sagaId -> {
      c.accept(sagaId);
      return NoResult.instance();
    };
  }

  public CheckedFunction<String, NoResult> runnableToCheckedFunc(Runnable r) {
    return sagaId -> voidRunnableToCallable(r).call();
  }
}
