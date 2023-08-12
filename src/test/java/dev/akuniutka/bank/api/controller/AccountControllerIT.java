package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
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
    private static final Long EXISTING_USER = 1001L;
    private static final Long NON_EXISTING_USER = 1003L;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private AccountService service;
    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        service.setUserBalance(EXISTING_USER, BigDecimal.TEN);
    }

    @Test
    void testGetBalanceWhenUserExists() throws Exception {
        BigDecimal result = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
        ResponseDto response = new ResponseDto(result);
        String expected = objectMapper.writeValueAsString(response);
        webTestClient
                .get()
                .uri(GET_BALANCE, EXISTING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetBalanceWhenUserDoesNotExist() throws Exception {
        ResponseDto response = new ResponseDto(BigDecimal.ONE.negate(), USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        webTestClient
                .get()
                .uri(GET_BALANCE, NON_EXISTING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsPositive() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.TEN);
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
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
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(amount);
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
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
    }

    @Test
    void testPutMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(amount);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, WRONG_MINOR_UNITS);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.ZERO);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_ZERO);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.TEN.negate());
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_NEGATIVE);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(NON_EXISTING_USER);
        order.setAmount(BigDecimal.TEN);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setAmount(BigDecimal.TEN);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.ONE);
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
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
    }

    @Test
    void testTakeMoneyWhenUserExistsAndAmountIsEqualToBalance() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.TEN);
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
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
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoButWithZeros() throws Exception {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(amount);
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
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
    }

    @Test
    void testTakeMoneyWhenUserExistsAndScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        BigDecimal amount = BigDecimal.ONE
                .setScale(3, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP)
                .divide(BigDecimal.TEN, RoundingMode.HALF_UP);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(amount);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, WRONG_MINOR_UNITS);
        String expected = objectMapper.writeValueAsString(response);
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
        service.setUserBalance(EXISTING_USER, BigDecimal.ONE);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.TEN);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, INSUFFICIENT_BALANCE);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.ZERO);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_ZERO);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.ONE.negate());
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_NEGATIVE);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, AMOUNT_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setUserId(NON_EXISTING_USER);
        order.setAmount(BigDecimal.ONE);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
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
        CashOrderDto order = new CashOrderDto();
        order.setAmount(BigDecimal.ONE);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
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
}