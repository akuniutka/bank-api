package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;
import static dev.akuniutka.bank.api.util.DateChecker.isDateBetween;
import static dev.akuniutka.bank.api.util.WebTestClientWrapper.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BalanceControllerIT {
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String GET_OPERATIONS = "/getOperationList/{userId}?dateFrom={dateFrom}&dateTo={dateTo}";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetBalanceWhenUserExists() throws Exception {
        Long userId = 1053L;
        String expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        String expected = jsonResponseFrom(MINUS_ONE, USER_NOT_FOUND);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsPositive() throws Exception {
        Long userId = 1054L;
        CashOrderDto order = cashOrderFrom(userId, TEN);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        get(webTestClient, GET_OPERATIONS, userId, dateFrom, dateTo)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.DEPOSIT.getDescription())
                .jsonPath("$[0].amount").isEqualTo(TEN.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        Long userId = 1055L;
        CashOrderDto order = cashOrderFrom(userId, TEN_THOUSANDTHS);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN_THOUSANDTHS);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        get(webTestClient, GET_OPERATIONS, userId, dateFrom, dateTo)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.DEPOSIT.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN_THOUSANDTHS)
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        Long userId = 1056L;
        CashOrderDto order = cashOrderFrom(userId, ONE_THOUSANDTH);
        String expected = jsonResponseFrom(ZERO, WRONG_MINOR_UNITS);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsZero() throws Exception {
        Long userId = 1057L;
        CashOrderDto order = cashOrderFrom(userId, ZERO);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_ZERO);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsNegative() throws Exception {
        Long userId = 1058L;
        CashOrderDto order = cashOrderFrom(userId, MINUS_TEN);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NEGATIVE);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsNull() throws Exception {
        Long userId = 1059L;
        CashOrderDto order = cashOrderFrom(userId, NULL);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NULL);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        CashOrderDto order = cashOrderFrom(userId, TEN);
        String expected = jsonResponseFrom(ZERO, USER_NOT_FOUND);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserIdIsNull() throws Exception {
        CashOrderDto order = cashOrderFrom(null, TEN);
        String expected = jsonResponseFrom(ZERO, USER_ID_IS_NULL);
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsLessThatBalance() throws Exception {
        Long userId = 1060L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        get(webTestClient, GET_OPERATIONS, userId, dateFrom, dateTo)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.WITHDRAWAL.getDescription())
                .jsonPath("$[0].amount").isEqualTo(ONE.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsEqualToBalance() throws Exception {
        Long userId = 1061L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_ZERO);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        get(webTestClient, GET_OPERATIONS, userId, dateFrom, dateTo)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.WITHDRAWAL.getDescription())
                .jsonPath("$[0].amount").isEqualTo(ONE.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        Long userId = 1062L;
        CashOrderDto order = cashOrderFrom(userId, TEN_THOUSANDTHS);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        get(webTestClient, GET_OPERATIONS, userId, dateFrom, dateTo)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.WITHDRAWAL.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN_THOUSANDTHS)
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        Long userId = 1063L;
        CashOrderDto order = cashOrderFrom(userId, ONE_THOUSANDTH);
        String expected = jsonResponseFrom(ZERO, WRONG_MINOR_UNITS);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsGreaterThanBalance() throws Exception {
        Long userId = 1064L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ZERO, INSUFFICIENT_BALANCE);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsZero() throws Exception {
        Long userId = 1065L;
        CashOrderDto order = cashOrderFrom(userId, ZERO);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_ZERO);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsNegative() throws Exception {
        Long userId = 1066L;
        CashOrderDto order = cashOrderFrom(userId, MINUS_ONE);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NEGATIVE);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsNull() throws Exception {
        Long userId = 1067L;
        CashOrderDto order = cashOrderFrom(userId, NULL);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NULL);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ZERO, USER_NOT_FOUND);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserIdIsNull() throws Exception {
        CashOrderDto order = cashOrderFrom(null, ONE);
        String expected = jsonResponseFrom(ZERO, USER_ID_IS_NULL);
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    private CashOrderDto cashOrderFrom(Long userId, BigDecimal amount) {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(userId);
        order.setAmount(amount);
        return order;
    }

    private String jsonResponseFrom(BigDecimal result, String message) throws Exception {
        ResponseDto response = new ResponseDto(result, message);
        return objectMapper.writeValueAsString(response);
    }

    private String jsonResponseFrom(BigDecimal result) throws Exception {
        ResponseDto response = new ResponseDto(result);
        return objectMapper.writeValueAsString(response);
    }
}