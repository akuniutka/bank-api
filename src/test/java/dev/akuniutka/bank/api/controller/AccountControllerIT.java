package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerIT {
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String USER_ID_IS_NULL = "user id is null";
    private static final String USER_NOT_FOUND = "user not found";
    private static final String AMOUNT_IS_NULL = "amount is null";
    private static final String AMOUNT_IS_ZERO = "amount is zero";
    private static final String AMOUNT_IS_NEGATIVE = "amount is negative";
    private static final String WRONG_MINOR_UNITS = "wrong minor units";
    private static final String INSUFFICIENT_BALANCE = "insufficient balance";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetBalanceWhenUserExists() throws Exception {
        Long userId = 1003L;
        BigDecimal result = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
        String expected = jsonResponseFrom(result);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        String expected = jsonResponseFrom(BigDecimal.ONE.negate(), USER_NOT_FOUND);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsPositive() throws Exception {
        Long userId = 1004L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.TEN);
        String expected = jsonResponseFrom(BigDecimal.ONE);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        BigDecimal result = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
        expected = jsonResponseFrom(result);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        Long userId = 1005L;
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = cashOrderFrom(userId, amount);
        String expected = jsonResponseFrom(BigDecimal.ONE);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(amount.setScale(2, RoundingMode.HALF_UP));
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        Long userId = 1006L;
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = cashOrderFrom(userId, amount);
        String expected = jsonResponseFrom(BigDecimal.ZERO, WRONG_MINOR_UNITS);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsZero() throws Exception {
        Long userId = 1007L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ZERO);
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_ZERO);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsNegative() throws Exception {
        Long userId = 1008L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.TEN.negate());
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_NEGATIVE);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsNull() throws Exception {
        Long userId = 1009L;
        CashOrderDto order = cashOrderFrom(userId, null);
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_NULL);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.TEN);
        String expected = jsonResponseFrom(BigDecimal.ZERO, USER_NOT_FOUND);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserIdIsNull() throws Exception {
        CashOrderDto order = cashOrderFrom(null, BigDecimal.TEN);
        String expected = jsonResponseFrom(BigDecimal.ZERO, USER_ID_IS_NULL);
        webTestClient
                .put()
                .uri(PUT_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsLessThatBalance() throws Exception {
        Long userId = 1010L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ONE);
        String expected = jsonResponseFrom(BigDecimal.ONE);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP));
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsEqualToBalance() throws Exception {
        Long userId = 1011L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ONE);
        String expected = jsonResponseFrom(BigDecimal.ONE);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        Long userId = 1012L;
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = cashOrderFrom(userId, amount);
        String expected = jsonResponseFrom(BigDecimal.ONE);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP));
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        Long userId = 1013L;
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = cashOrderFrom(userId, amount);
        String expected = jsonResponseFrom(BigDecimal.ZERO, WRONG_MINOR_UNITS);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsGreaterThanBalance() throws Exception {
        Long userId = 1014L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ONE);
        String expected = jsonResponseFrom(BigDecimal.ZERO, INSUFFICIENT_BALANCE);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsZero() throws Exception {
        Long userId = 1015L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ZERO);
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_ZERO);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsNegative() throws Exception {
        Long userId = 1016L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ONE.negate());
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_NEGATIVE);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsNull() throws Exception {
        Long userId = 1017L;
        CashOrderDto order = cashOrderFrom(userId, null);
        String expected = jsonResponseFrom(BigDecimal.ZERO, AMOUNT_IS_NULL);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        CashOrderDto order = cashOrderFrom(userId, BigDecimal.ONE);
        String expected = jsonResponseFrom(BigDecimal.ZERO, USER_NOT_FOUND);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTakeMoneyWhenUserIdIsNull() throws Exception {
        CashOrderDto order = cashOrderFrom(null, BigDecimal.ONE);
        String expected = jsonResponseFrom(BigDecimal.ZERO, USER_ID_IS_NULL);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
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