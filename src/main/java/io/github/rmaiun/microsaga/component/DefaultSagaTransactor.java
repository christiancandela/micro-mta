package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.exception.SagaActionFailedException;
import io.github.rmaiun.microsaga.exception.SagaCompensationFailedException;
import io.github.rmaiun.microsaga.func.CheckedFunction;
import io.github.rmaiun.microsaga.func.StubInputFunction;
import io.github.rmaiun.microsaga.mta.*;
import io.github.rmaiun.microsaga.support.Evaluation;
import io.github.rmaiun.microsaga.support.EvaluationHistory;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.support.FunctionContext;
import net.jodah.failsafe.Failsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Function;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class DefaultSagaTransactor implements SagaTransactor {

  @Override
  public <A> EvaluationResult<A> transact(String sagaId, MTA<A> saga) {
    return run(sagaId, saga);
  }

  @SuppressWarnings("unchecked")
  public <X> EvaluationResult<X> run(String sagaId, MTA<X> sagaInput) {
    Stack<Compensation> compensations = new Stack<>();
    Stack<FunctionContext> sagaDefinitions = new Stack<>();
    List<Evaluation<?>> evaluations = new ArrayList<>();
    Function<Object, MTA<Object>> sagaInputFunc = (StubInputFunction<MTA<Object>>) o -> (MTA<Object>) sagaInput;
    sagaDefinitions.add(new FunctionContext(sagaInputFunc));
    EvaluationResult<Object> current = EvaluationResult.failed(new IllegalArgumentException("Empty saga defined"));
    boolean noError = true;
    while (!sagaDefinitions.empty() && noError) {
      FunctionContext ctx = sagaDefinitions.pop();
      BiFunction<Object, Object, Object> transformer = ctx.getTransformer();
      Function<Object, MTA<Object>> f = ctx.getSagaFunction();
      MTA<Object> saga = f instanceof StubInputFunction
          ? f.apply(null)
          : f.apply(current.getValue());
      if (saga instanceof Success) {
        current = EvaluationResult.success(((Success<X>) saga).getValue());
        noError = !current.isError();
      } else if (saga instanceof Action) {
        Action<Object> a = (Action<Object>) saga;
        current = evaluateStep(sagaId, current.getValue(), a.withoutCompensation(), compensations, evaluations, transformer);
        noError = !current.isError();
      } else if (saga instanceof Step) {
        Step<Object> sagaStep = (Step<Object>) saga;
        current = evaluateStep(sagaId, current.getValue(), sagaStep, compensations, evaluations, transformer);
        noError = !current.isError();
      } else if (saga instanceof TransformedFlatMap) {
        TransformedFlatMap<Object, Object, Object> sagaTransFlatMap = (TransformedFlatMap<Object, Object, Object>) saga;
        sagaDefinitions.add(new FunctionContext(sagaTransFlatMap.getSagaFunc(), sagaTransFlatMap.getTransformer()));
        sagaDefinitions.add(new FunctionContext(sagaTransFlatMap.getRootSaga()));
      } else if (saga instanceof FlatMap) {
        FlatMap<Object, Object> sagaFlatMap = (FlatMap<Object, Object>) saga;
        sagaDefinitions.add(new FunctionContext(sagaFlatMap.getfB()));
        sagaDefinitions.add(new FunctionContext(sagaFlatMap.getA()));
      } else {
        current = EvaluationResult.failed(new IllegalArgumentException("Invalid Nested Operation"));
        noError = !current.isError();
      }
    }
    return new EvaluationResult<>((X) current.getValue(), new EvaluationHistory(sagaId, evaluations), current.getError());
  }

  private EvaluationResult<Object> evaluateStep(String sagaId, Object prevValue, Step<Object> sagaStep,
      Stack<Compensation> compensations, List<Evaluation<?>> evaluations,
      BiFunction<Object, Object, Object> transformer) {
    CheckedFunction<String, Object> action = sagaStep.getAction().getAction();
    compensations.add(sagaStep.getCompensator());
    long actionStart = System.currentTimeMillis();
    String actionName = sagaStep.getAction().getName();
    try {
      Object callResult = Failsafe.with(sagaStep.getAction().getRetryPolicy()).get(() -> action.apply(sagaId));
      String path = callResult.getClass().getName();
      evaluations.add(Evaluation.action(actionName, System.currentTimeMillis() - actionStart, true, callResult, path));
      Object finalResult = transformer == null
          ? callResult
          : transformer.apply(prevValue, callResult);
      return EvaluationResult.success(finalResult);
    } catch (Throwable ta) {
      evaluations.add(Evaluation.action(actionName, System.currentTimeMillis() - actionStart, false, ta.toString(), null));
      long compensationStart = System.currentTimeMillis();
      String compensation = null;
      try {
        while (!compensations.empty()) {
          Compensation pop = compensations.pop();
          if (!pop.isTechnical()) {
            compensationStart = System.currentTimeMillis();
            compensation = pop.getName();
            Failsafe.with(pop.getRetryPolicy()).run(() -> pop.getCompensation().accept(sagaId));
            evaluations.add(Evaluation.compensation(compensation, System.currentTimeMillis() - compensationStart, true, null));
          }
        }
      } catch (Throwable tc) {
        evaluations.add(Evaluation.compensation(compensation, System.currentTimeMillis() - compensationStart, false, tc.getMessage()));
        return EvaluationResult.failed(new SagaCompensationFailedException(compensation, sagaId, tc));
      }
      return EvaluationResult.failed(new SagaActionFailedException(actionName, sagaId, ta));
    }
  }
}
