package io.github.rmaiun.microsaga.helper;

import io.github.rmaiun.microsaga.Sagas;
import io.github.rmaiun.microsaga.component.MTAManager;
import io.github.rmaiun.microsaga.dto.CreateOrderDto;
import io.github.rmaiun.microsaga.dto.PaymentRequest;
import io.github.rmaiun.microsaga.dto.ProductOrder;
import io.github.rmaiun.microsaga.saga.SagaStep;
import io.github.rmaiun.microsaga.services.BusinessLogger;
import io.github.rmaiun.microsaga.services.DeliveryService;
import io.github.rmaiun.microsaga.services.MoneyTransferService;
import io.github.rmaiun.microsaga.services.OrderService;
import io.github.rmaiun.microsaga.support.Evaluation;
import io.github.rmaiun.microsaga.support.EvaluationHistory;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.support.NoResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class CreateOrderHelper {

  private static final Logger LOG = LogManager.getLogger(CreateOrderHelper.class);
  private final OrderService orderService;
  private final MoneyTransferService moneyTransferService;
  private final DeliveryService deliveryService;
  private final BusinessLogger businessLogger;

  public CreateOrderHelper(OrderService orderService, MoneyTransferService moneyTransferService, DeliveryService deliveryService,
      BusinessLogger businessLogger) {
    this.orderService = orderService;
    this.moneyTransferService = moneyTransferService;
    this.deliveryService = deliveryService;
    this.businessLogger = businessLogger;
  }

  public void createOrder(CreateOrderDto dto) {
    SagaStep<ProductOrder> orderSagaPart = Sagas.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Sagas.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    SagaStep<NoResult> paymentSagaPart = Sagas
        .action("processPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), 100)))
        .compensate(
            Sagas.compensation("cancelPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), -100))));

    SagaStep<NoResult> deliverySagaPart = Sagas.action("registerDelivery", () -> deliveryService.registerDelivery(dto.getPerson()))
        .withoutCompensation();

    new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact()
        .valueOrThrow();
  }

  public void createOrdersWithFailedDelivery(CreateOrderDto dto) {
    SagaStep<ProductOrder> orderSagaPart = Sagas.action("makeOrder", () -> orderService.makeOrder(dto.getProduct()))
        .compensate(Sagas.compensation("cancelOrder", () -> orderService.cancelOrder(dto.getProduct())));

    SagaStep<NoResult> paymentSagaPart = Sagas
        .action("processPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), 100)))
        .compensate(
            Sagas.compensation("cancelPayment", () -> moneyTransferService.processPayment(new PaymentRequest(dto.getPerson(), -100))));

    SagaStep<NoResult> deliverySagaPart = Sagas
        .action("registerDelivery", () -> deliveryService.registerDeliveryWithWrongAddress(dto.getPerson()))
        .compensate(Sagas.compensation("failedDeliveryBusinessLog",
            () -> businessLogger.createBusinessLog("Delivery planning was failed for user " + dto.getPerson())));

    EvaluationResult<NoResult> result = new MTAManager()
        .saga(orderSagaPart.then(paymentSagaPart).then(deliverySagaPart))
        .withId("testSaga")
        .transact();
    logSaga(result.getEvaluationHistory());
  }

  private void logSaga(EvaluationHistory evaluationHistory) {
    for (Evaluation<?> e : evaluationHistory.getEvaluations()) {
      LOG.info("SAGA:{} [{}:{}] {} {}(ms)", evaluationHistory.getSagaId(), e.getEvaluationType().name().toLowerCase(),
          e.isSuccess() ? "success" : "failed", e.getName(), e.getDuration());
    }
  }
}
