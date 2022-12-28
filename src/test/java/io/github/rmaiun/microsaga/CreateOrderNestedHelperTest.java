package io.github.rmaiun.microsaga;

import io.github.rmaiun.microsaga.dto.CreateOrderDto;
import io.github.rmaiun.microsaga.helper.CreateNestedOrderHelper;
import io.github.rmaiun.microsaga.services.*;
import io.github.rmaiun.microsaga.support.Evaluation;
import io.github.rmaiun.microsaga.support.EvaluationResult;
import io.github.rmaiun.microsaga.support.NoResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
/**
 * Original creado por Roman Maiun
 * Modificado por Christian Candela
 */
public class CreateOrderNestedHelperTest {

  public static final String IPHONE_X = "Iphone X";
  public static final int DEFAULT_PRICE = 1000;
  public static final int DEFAULT_QTY = 10;
  public static final String USER_1 = "user1";

  @Test
  public void happyDayActionsTest() {

    Catalog catalog = new Catalog();
    catalog.addProduct("Samsung Galaxy", 10);
    catalog.addProduct(IPHONE_X, DEFAULT_QTY);
    OrderService orderService = new OrderService(catalog);

    HashMap<String, Integer> accounts = new HashMap<>();
    accounts.put(USER_1, DEFAULT_PRICE);
    accounts.put("user2", 1000);
    MoneyTransferService moneyTransferService = new MoneyTransferService(accounts);

    DeliveryService deliveryService = new DeliveryService();
    BusinessLogger businessLogger = new BusinessLogger();

    CreateNestedOrderHelper createOrderHelper = new CreateNestedOrderHelper(orderService, moneyTransferService, deliveryService, businessLogger);
    createOrderHelper.createOrder(new CreateOrderDto("user1", "Iphone X"));
    assertEquals(900, accounts.get(USER_1));
    assertEquals(9, catalog.getProduct(IPHONE_X));
  }

  @Test
  public void retryActionsTest() {
    Catalog catalog = new Catalog();
    catalog.addProduct("Samsung Galaxy", 10);
    catalog.addProduct(IPHONE_X, DEFAULT_QTY);
    OrderService orderService = new OrderService(catalog);

    HashMap<String, Integer> accounts = new HashMap<>();
    accounts.put(USER_1, DEFAULT_PRICE);
    accounts.put("user2", 1000);
    MoneyTransferService moneyTransferService = new MoneyTransferService(accounts);

    DeliveryService deliveryService = new DeliveryService();
    BusinessLogger businessLogger = new BusinessLogger();

    CreateNestedOrderHelper createOrderHelper = new CreateNestedOrderHelper(orderService, moneyTransferService, deliveryService, businessLogger);
    EvaluationResult<NoResult> result = createOrderHelper.createOrdersWithRetryAction(new CreateOrderDto("user1", "Iphone X"));
    int results = 0;
    for (Evaluation<?> e : result.getEvaluationHistory().getEvaluations()) {
      if (e.getResult().getData() != null) {
        results++;
      }
    }
    assertEquals(900, accounts.get(USER_1));
    assertEquals(9, catalog.getProduct(IPHONE_X));
    assertEquals(3, results);
  }

  @Test
  public void activatedCompensationsTest() {
    Catalog catalog = new Catalog();
    catalog.addProduct("Samsung Galaxy", 10);
    catalog.addProduct(IPHONE_X, DEFAULT_QTY);
    OrderService orderService = new OrderService(catalog);

    HashMap<String, Integer> accounts = new HashMap<>();
    accounts.put(USER_1, DEFAULT_PRICE);
    accounts.put("user2", 1000);
    MoneyTransferService moneyTransferService = new MoneyTransferService(accounts);

    DeliveryService deliveryService = new DeliveryService();
    BusinessLogger businessLogger = new BusinessLogger();

    CreateNestedOrderHelper createOrderHelper = new CreateNestedOrderHelper(orderService, moneyTransferService, deliveryService, businessLogger);
    createOrderHelper.createOrdersWithFailedDelivery(new CreateOrderDto("user1", "Iphone X"));
    assertEquals(1000, accounts.get(USER_1));
    assertEquals(10, catalog.getProduct(IPHONE_X));
  }

  @Test
  public void activatedRetriedCompensationsTest() {
    Catalog catalog = new Catalog();
    catalog.addProduct("Samsung Galaxy", 10);
    catalog.addProduct(IPHONE_X, DEFAULT_QTY);
    OrderService orderService = new OrderService(catalog);

    HashMap<String, Integer> accounts = new HashMap<>();
    accounts.put(USER_1, DEFAULT_PRICE);
    accounts.put("user2", 1000);
    MoneyTransferService moneyTransferService = new MoneyTransferService(accounts);

    DeliveryService deliveryService = new DeliveryService();
    BusinessLogger businessLogger = new BusinessLogger();

    CreateNestedOrderHelper createOrderHelper = new CreateNestedOrderHelper(orderService, moneyTransferService, deliveryService, businessLogger);
    createOrderHelper.createOrdersWithRetryCompensation(new CreateOrderDto("user1", "Iphone X"));
    assertEquals(1000, accounts.get(USER_1));
    assertEquals(10, catalog.getProduct(IPHONE_X));
  }
}
