package io.github.rmaiun.microsaga.helper;

import io.github.rmaiun.microsaga.Nesteds;
import io.github.rmaiun.microsaga.component.MTAManager;
import io.github.rmaiun.microsaga.dto.CreateOrderDto;
import io.github.rmaiun.microsaga.dto.PaymentRequest;
import io.github.rmaiun.microsaga.dto.ProductOrder;
import io.github.rmaiun.microsaga.mta.Step;
import io.github.rmaiun.microsaga.services.BusinessLogger;
import io.github.rmaiun.microsaga.services.DeliveryService;
import io.github.rmaiun.microsaga.services.MoneyTransferService;
import io.github.rmaiun.microsaga.services.OrderService;
import io.github.rmaiun.microsaga.support.Evaluation;
import io.github.rmaiun.microsaga.support.EvaluationHistory;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.support.NoResult;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class CreateNestedOrderHelper {

  private static final Logger LOG = LogManager.getLogger(CreateNestedOrderHelper.class);
  private final OrderService orderService;
  private final MoneyTransferService moneyTransferService;
  private final DeliveryService deliveryService;
  private final BusinessLogger businessLogger;

  public CreateNestedOrderHelper(OrderService orderService, MoneyTransferService moneyTransferService, DeliveryService deliveryService,
                                 BusinessLogger businessLogger) {
    this.orderService = orderService;
    this.moneyTransferService = moneyTransferService;
    this.deliveryService = deliveryService;
    this.businessLogger = businessLogger;
  }

  public void createOrder(CreateOrderDto dto) {
    Step<ProductOrder> orderSagaPart = Nesteds.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Nesteds.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    Step<NoResult> paymentSagaPart = Nesteds
        .action("processPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), 100)))
        .compensate(
            Nesteds.compensation("cancelPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), -100))));

    Step<NoResult> deliverySagaPart = Nesteds.action("registerDelivery", () -> deliveryService.registerDelivery(dto.getPerson()))
        .withoutCompensation();

    new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact()
        .valueOrThrow();
  }

  public void createOrdersWithFailedDelivery(CreateOrderDto dto) {
    Step<ProductOrder> orderSagaPart = Nesteds.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Nesteds.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    Step<NoResult> paymentSagaPart = Nesteds
        .action("processPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), 100)))
        .compensate(
            Nesteds.compensation("cancelPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), -100))));

    Step<NoResult> deliverySagaPart = Nesteds
        .action("registerDelivery", () -> deliveryService.registerDeliveryWithWrongAddress(dto.getPerson()))
        .compensate(Nesteds.compensation("failedDeliveryBusinessLog",
            () -> businessLogger.createBusinessLog("Delivery planning was failed for user " + dto.getPerson())));

    EvaluationResult<NoResult> result = new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact();
    logSaga(result.getEvaluationHistory());
  }

  public void createOrdersWithRetryCompensation(CreateOrderDto dto) {
    Step<ProductOrder> orderSagaPart = Nesteds.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Nesteds.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    Step<NoResult> paymentSagaPart = Nesteds
        .action("processPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), 100)))
        .compensate(Nesteds.retryableCompensation("cancelPayment (lagging)",
            () -> moneyTransferService.processLaggingPayment(new PaymentRequest(dto.getPerson(), -100)),
            new RetryPolicy<>().withMaxRetries(4)));

    Step<NoResult> deliverySagaPart = Nesteds
        .action("registerDelivery", () -> deliveryService.registerDeliveryWithWrongAddress(dto.getPerson()))
        .compensate(Nesteds.compensation("failedDeliveryBusinessLog",
            () -> businessLogger.createBusinessLog("Delivery planning was failed for user " + dto.getPerson())));

    EvaluationResult<NoResult> result = new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact();
    logSaga(result.getEvaluationHistory());
  }

  public EvaluationResult<NoResult> createOrdersWithRetryAction(CreateOrderDto dto) {
    Step<ProductOrder> orderSagaPart = Nesteds.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Nesteds.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    Step<NoResult> paymentSagaPart = Nesteds
        .retryableAction("processLaggingPayment",
            () -> moneyTransferService.processLaggingPayment(new PaymentRequest(dto.getPerson(), 100)),
            new RetryPolicy<NoResult>().withMaxRetries(4))
        .compensate(Nesteds.compensation("cancelPayment (lagging)",
            () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), -100))));

    Step<NoResult> deliverySagaPart = Nesteds.action("registerDelivery", () -> deliveryService.registerDelivery(dto.getPerson()))
        .compensate(Nesteds.compensation("failedDeliveryBusinessLog",
            () -> businessLogger.createBusinessLog("Delivery planning was failed for user " + dto.getPerson())));

    EvaluationResult<NoResult> result = new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact();
    logSaga(result.getEvaluationHistory());
    return result;
  }

  private void logSaga(EvaluationHistory evaluationHistory) {
    for (Evaluation<?> e : evaluationHistory.getEvaluations()) {
      LOG.info("SAGA:{} [{}:{}] {} {}(ms)", evaluationHistory.getSagaId(), e.getEvaluationType().name().toLowerCase(),
          e.isSuccess() ? "success" : "failed", e.getName(), e.getDuration());
    }
  }
}
