package io.github.rmaiun.microsaga;

import io.github.rmaiun.microsaga.component.MTAManager;
import io.github.rmaiun.microsaga.exception.MTAActionFailedException;
import io.github.rmaiun.microsaga.exception.MTACompensationFailedException;
import io.github.rmaiun.microsaga.mta.Action;
import io.github.rmaiun.microsaga.mta.MTA;
import io.github.rmaiun.microsaga.mta.Step;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.support.NoResult;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class CoreNestedTest {

  @Test
  public void flatmapTest() {
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Nesteds.action("initValue", x::incrementAndGet)
        .withoutCompensation();

    MTA<String> intToString = incrementStep
        .flatmap(a -> Sagas.action("intToString", () -> String.format("int=%d", a)).withoutCompensation());
    EvaluationResult<String> result = MTAManager.use(intToString).transact();
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
    assertEquals("int=1", result.getValue());
  }

  @Test
  public void mapFlatmapTest() {
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Sagas.action("initValue", x::incrementAndGet)
        .withoutCompensation();

    MTA<Integer> intToString = incrementStep
        .flatmap(a -> Nesteds.action("intToString", () -> String.format("int=%d", a)).withoutCompensation())
        .map(str -> str.split("=").length);
    EvaluationResult<Integer> result = MTAManager.use(intToString).transact();
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
    assertEquals(2, result.getValue());
  }

  @Test
  public void compensatedFlatmapTest() {
    AtomicInteger x = new AtomicInteger();
    Callable<Integer> throwError = () -> {
      if (x.get() == 0) {
        throw new RuntimeException("Wrong value");
      } else {
        return x.incrementAndGet();
      }
    };

    Step<Integer> incrementStep = Nesteds.action("initValue", throwError)
        .compensate("setAtomicIntToZero", () -> x.set(0));

    MTA<Integer> intToString = incrementStep
        .flatmap(a -> Nesteds.action("intToString", () -> String.format("int=%d", a)).withoutCompensation())
        .map(str -> str.split("=").length);
    EvaluationResult<Integer> result = MTAManager.use(intToString).transact();
    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertEquals(MTAActionFailedException.class, result.getError().getClass());
  }

  @Test
  public void compensationFailedFlatmapTest() {
    AtomicInteger x = new AtomicInteger();
    Callable<Integer> throwError = () -> {
      if (x.get() == 0) {
        throw new RuntimeException("Wrong value");
      } else {
        return x.incrementAndGet();
      }
    };
    Step<Integer> incrementStep = Nesteds.action("initValue", throwError)
        .compensate("setAtomicIntToZero", () -> {
          throw new RuntimeException("something wrong");
        });

    MTA<Integer> intToString = incrementStep
        .flatmap(a -> Nesteds.action("intToString", () -> String.format("int=%d", a)).withoutCompensation())
        .map(str -> str.split("=").length);
    EvaluationResult<Integer> result = MTAManager.use(intToString).transact();
    assertNotNull(result);
    assertFalse(result.isSuccess());
    assertEquals(MTACompensationFailedException.class, result.getError().getClass());
  }

  @Test
  public void thenTest() {
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Nesteds.action("initValue", x::incrementAndGet)
        .withoutCompensation();

    MTA<String> intToString = incrementStep
        .then(Nesteds.action("intToString", () -> String.format("int=%d", 1)).withoutCompensation())
        .then(Nesteds.action("intToString", () -> String.format("int=%d", 2)).withoutCompensation())
        .then(Nesteds.action("intToString", () -> String.format("int=%d", 3)).withoutCompensation());
    EvaluationResult<String> result = MTAManager.use(intToString).transact();
    assertNotNull(result);
    assertTrue(result.isSuccess());
    assertNotNull(result.getValue());
    assertEquals("int=3", result.getValue());
  }

  @Test
  public void zipWithTest1() {
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Nesteds.action("initValue", x::incrementAndGet)
        .withoutCompensation();
    MTA<Integer> intToString = incrementStep
        .zipWith(a -> Nesteds.action("intToString", () -> String.format("int=%d", a)).withoutCompensation(), (a, b) -> a)
        .flatmap(a -> Nesteds.action("+3", () -> a + 3).withoutCompensation());
    Integer result = MTAManager.use(intToString).transact().valueOrThrow();
    assertNotNull(result);
    assertEquals(4, result);
  }

  @Test
  public void zipWithTest2() {
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Nesteds.action("initValue", x::incrementAndGet)
        .withoutCompensation();
    MTA<String> intToString = incrementStep
        .zipWith(Nesteds.action("intToString", () -> String.format("int=%d", 14)).withoutCompensation(), a -> a + 15)
        .flatmap(a -> Nesteds.action("+3", () -> a + 3).withoutCompensation());
    String result = MTAManager.use(intToString).transact().valueOrThrow();
    assertNotNull(result);
    assertEquals("int=14153", result);
  }

  @Test
  public void evaluationResultFlatTapTest() {
    List<Integer> list = new ArrayList<>();
    AtomicInteger x = new AtomicInteger();
    Step<Integer> incrementStep = Nesteds.action("initValue", x::incrementAndGet)
        .withoutCompensation();
    MTA<String> intToString = incrementStep
        .zipWith(Nesteds.action("intToString", () -> String.format("int=%d", 14)).withoutCompensation(), a -> a + 15)
        .flatmap(a -> Nesteds.action("+3", () -> a + 3).withoutCompensation());
    String result = MTAManager.use(intToString).transact()
        .peek(er -> list.add(er.getEvaluationHistory().getEvaluations().size()))
        .valueOrThrow();
    assertNotNull(result);
    assertEquals("int=14153", result);
    assertNotNull(list);
    assertNotNull(list.get(0));
    assertEquals(3, list.get(0));
  }

  @Test
  public void voidActionTest() {
    AtomicReference<String> ref = new AtomicReference<>();
    Step<NoResult> saga = Nesteds.voidAction("action#1", () -> {
      throw new RuntimeException("action#1 failed");
    })
        .compensate("compensation#1", ref::set);
    EvaluationResult<NoResult> evaluationResult = MTAManager.use(saga).transact();
    assertNotNull(evaluationResult);
    assertEquals(evaluationResult.getEvaluationHistory().getSagaId(), ref.get());
  }

  @Test
  public void voidRetriableActionTest() {
    AtomicInteger ref = new AtomicInteger(0);
    Step<NoResult> saga = Nesteds.voidRetryableAction("action#1", () -> {
      ref.incrementAndGet();
      throw new RuntimeException("action#1 failed");
    }, new RetryPolicy<NoResult>().withMaxRetries(2))
        .compensate("compensation#1", ref::decrementAndGet);
    EvaluationResult<NoResult> evaluationResult = MTAManager.use(saga).transact();
    assertNotNull(evaluationResult);
    assertTrue(evaluationResult.isError());
    assertEquals(2, ref.get());
  }

  @Test
  public void actionConsumesSagaIdTest() {
    AtomicReference<String> ref = new AtomicReference<>();
    MTA<NoResult> saga = Nesteds.voidAction("a1", ref::set)
        .withoutCompensation();
    EvaluationResult<NoResult> evaluationResult = MTAManager.use(saga).transact();
    assertNotNull(evaluationResult);
    assertTrue(evaluationResult.isSuccess());
    assertEquals(evaluationResult.getEvaluationHistory().getSagaId(), ref.get());
  }

  @Test
  public void repeatableCompensationConsumesSagaIdTest() {
    AtomicReference<String> ref = new AtomicReference<>("");
    Step<NoResult> saga = Nesteds.voidAction("action#1", () -> {
      throw new RuntimeException("action#1 failed");
    })
        .compensate("compensation#1", ref::set, new RetryPolicy<>().withMaxRetries(2));
    EvaluationResult<NoResult> evaluationResult = MTAManager.use(saga).transact();
    assertNotNull(evaluationResult);
    String sagaId = evaluationResult.getEvaluationHistory().getSagaId();
    assertEquals(sagaId, ref.get());
  }

  @Test
  public void actionThrowsTest() {
    assertThrows(MTAActionFailedException.class,
        () -> MTAManager.use(Nesteds.actionThrows("a1", new RuntimeException("action a1 is failed"))
            .compensate(Nesteds.emptyCompensation("c1")))
            .transact()
            .orElseThrow());
  }

  @Test
  public void emptyCompensationTest() {
    MTA<Object> saga = Nesteds.actionThrows("a1", new RuntimeException("action a1 is failed"))
        .compensate(Nesteds.emptyCompensation("c1"));
    EvaluationResult<Object> er = MTAManager.use(saga).transact();
    assertEquals(2, er.getEvaluationHistory().getEvaluations().size());
    boolean allEvaluationsPresent = er.getEvaluationHistory().getEvaluations()
        .stream()
        .allMatch(e -> Arrays.asList("a1", "c1").contains(e.getName()));
    assertTrue(allEvaluationsPresent);
  }

  @Test
  public void compensationThrowsTest() {
    MTA<Object> saga = Nesteds.actionThrows("a1", new RuntimeException("action a1 is failed"))
        .compensate(Nesteds.compensationThrows("c1", new RuntimeException("compensation c1 is failed")));
    EvaluationResult<Object> er = MTAManager.use(saga).transact();
    assertThrows(MTACompensationFailedException.class, er::orElseThrow);
  }

  @Test
  public void foldEvaluationResultTest() {
    MTA<Object> saga = Nesteds.actionThrows("a1", new RuntimeException("action a1 is failed"))
        .compensate(Nesteds.compensationThrows("c1", new RuntimeException("compensation c1 is failed")));
    String foldEvaluationResult = MTAManager.use(saga)
        .withId("foldEvaluationResultTest")
        .transact()
        .fold(v -> "no result", Throwable::getMessage);
    assertEquals("Compensation for saga foldEvaluationResultTest is failed while compensates c1 action", foldEvaluationResult);
  }

  @Test
  public void adaptErrorTest() {
    MTA<Object> saga = Nesteds.actionThrows("a1", new RuntimeException("action a1 is failed"))
        .compensate(Nesteds.compensationThrows("c1", new RuntimeException("compensation c1 is failed")));
    Object foldEvaluationResult = MTAManager.use(saga)
        .withId("foldEvaluationResultTest")
        .transact()
        .adaptError(err -> new NoResult())
        .valueOrThrow();
    assertNotNull(foldEvaluationResult);
  }

  @Test
  public void adoptErrorWithPeekTest() {
    Action<String> action = Nesteds.actionThrows("a1", new RuntimeException("action should fail"));
    MTA<String> saga = action.withoutCompensation();
    Function<RuntimeException, String> errorAdopter = err -> {
      if (err instanceof MTAActionFailedException) {
        return "default result";
      } else {
        throw err;
      }
    };
    AtomicInteger valueCounter = new AtomicInteger(0);
    AtomicInteger errorCounter = new AtomicInteger(0);
    String str = MTAManager.use(saga)
        .transact()
        .peek(evalRest -> valueCounter.incrementAndGet())
        .peekValue(v -> valueCounter.incrementAndGet())
        .peekError(err -> errorCounter.incrementAndGet())
        .adaptError(errorAdopter)
        .valueOrThrow();
    assertEquals("default result", str);
    assertEquals(1, errorCounter.get());
    assertEquals(1, valueCounter.get());
  }

  @Test
  public void wrongErrorAdoptionWithErrorTransformerTest() {
    class WrongAdoptionError extends RuntimeException {

      public WrongAdoptionError(String message, Throwable cause) {
        super(message, cause);
      }
    }
    Action<String> action = Nesteds.actionThrows("a1", new RuntimeException("action should fail"));
    MTA<String> saga = action.compensate(Nesteds.compensationThrows("c1", new RuntimeException("compensation is failed")));
    Function<RuntimeException, String> errorAdopter = err -> {
      if (err instanceof MTAActionFailedException) {
        return "default result";
      } else {
        throw err;
      }
    };
    WrongAdoptionError result = assertThrows(WrongAdoptionError.class, () -> MTAManager.use(saga)
        .transact()
        .adaptError(errorAdopter)
        .valueOrThrow(err -> new WrongAdoptionError("transformed error", err)));
    assertNotNull(result);
    assertEquals("transformed error", result.getMessage());
    assertEquals(MTACompensationFailedException.class, result.getCause().getClass());
  }
}
