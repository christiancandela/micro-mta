package io.github.rmaiun.microsaga.component;

import io.github.rmaiun.microsaga.exception.MTAActionFailedException;
import io.github.rmaiun.microsaga.exception.MTACompensationFailedException;
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
public class DefaultMTATransactor implements MTATransactor {

  @Override
  public <A> EvaluationResult<A> transact(String mtaId, MTA<A> mta) {
    return run(mtaId, mta);
  }

  @SuppressWarnings("unchecked")
  public <X> EvaluationResult<X> run(String mtaId, MTA<X> mtaInput) {
    Stack<Compensation> compensations = new Stack<>();
    Stack<FunctionContext> mtaDefinitions = new Stack<>();
    List<Evaluation<?>> evaluations = new ArrayList<>();
    Function<Object, MTA<Object>> sagaInputFunc = (StubInputFunction<MTA<Object>>) o -> (MTA<Object>) mtaInput;
    mtaDefinitions.add(new FunctionContext(sagaInputFunc));
    EvaluationResult<Object> current = EvaluationResult.failed(new IllegalArgumentException("Empty saga defined"));
    boolean noError = true;
    while (!mtaDefinitions.empty() && noError) {
      FunctionContext ctx = mtaDefinitions.pop();
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
        current = evaluateStep(mtaId, current.getValue(), a.withoutCompensation(), compensations, evaluations, transformer);
        noError = !current.isError();
      } else if (saga instanceof Step) {
        Step<Object> sagaStep = (Step<Object>) saga;
        current = evaluateStep(mtaId, current.getValue(), sagaStep, compensations, evaluations, transformer);
        noError = !current.isError();
      } else if (saga instanceof TransformedFlatMap) {
        TransformedFlatMap<Object, Object, Object> sagaTransFlatMap = (TransformedFlatMap<Object, Object, Object>) saga;
        mtaDefinitions.add(new FunctionContext(sagaTransFlatMap.getFunction(), sagaTransFlatMap.getTransformer()));
        mtaDefinitions.add(new FunctionContext(sagaTransFlatMap.getRootMTA()));
      } else if (saga instanceof FlatMap) {
        FlatMap<Object, Object> sagaFlatMap = (FlatMap<Object, Object>) saga;
        mtaDefinitions.add(new FunctionContext(sagaFlatMap.getfB()));
        mtaDefinitions.add(new FunctionContext(sagaFlatMap.getA()));
      } else {
        current = EvaluationResult.failed(new IllegalArgumentException("Invalid Nested Operation"));
        noError = !current.isError();
      }
    }
    return new EvaluationResult<>((X) current.getValue(), new EvaluationHistory(mtaId, evaluations), current.getError());
  }

  private EvaluationResult<Object> evaluateStep(String mtaId, Object prevValue, Step<Object> step,
      Stack<Compensation> compensations, List<Evaluation<?>> evaluations,
      BiFunction<Object, Object, Object> transformer) {
    CheckedFunction<String, Object> action = step.getAction().getAction();
    compensations.add(step.getCompensator());
    long actionStart = System.currentTimeMillis();
    String actionName = step.getAction().getName();
    try {
      Object callResult = Failsafe.with(step.getAction().getRetryPolicy()).get(() -> action.apply(mtaId));
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
            Failsafe.with(pop.getRetryPolicy()).run(() -> pop.getCompensation().accept(mtaId));
            evaluations.add(Evaluation.compensation(compensation, System.currentTimeMillis() - compensationStart, true, null));
          }
        }
      } catch (Throwable tc) {
        evaluations.add(Evaluation.compensation(compensation, System.currentTimeMillis() - compensationStart, false, tc.getMessage()));
        return EvaluationResult.failed(new MTACompensationFailedException(compensation, mtaId, tc));
      }
      return EvaluationResult.failed(new MTAActionFailedException(actionName, mtaId, ta));
    }
  }
}
