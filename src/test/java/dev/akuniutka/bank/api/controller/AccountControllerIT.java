package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerIT {
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String USER_NOT_FOUND = "user not found";
    private static final String INSUFFICIENT_BALANCE = "insufficient balance";
    private static final Long EXISTING_USER = 1001L;
    private static final Long NON_EXISTING_USER = 1003L;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private  WebTestClient webTestClient;


    @Test
    void testGetBalanceWhenUserExists() throws Exception {
        BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        ResponseDto response = new ResponseDto(balance);
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
        BigDecimal balance = BigDecimal.ONE.negate().setScale(2, RoundingMode.HALF_UP);
        ResponseDto response = new ResponseDto(balance, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        webTestClient
                .get()
                .uri(GET_BALANCE,  NON_EXISTING_USER)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testPutMoneyWhenUserExistsAndAmountIsPositive() throws Exception {
        ResponseDto response = new ResponseDto(BigDecimal.ONE);
        String expected = objectMapper.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        // TODO: replace with BigDecimal.TEN
        order.setAmount(BigDecimal.ONE);
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
    void testTakeMoneyWhenUserExistsAndAmountIsGreaterThanBalance() throws Exception {
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, INSUFFICIENT_BALANCE);
        String expected = objectMapper.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(EXISTING_USER);
        order.setAmount(BigDecimal.TEN);
        webTestClient
                .put()
                .uri(TAKE_MONEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }
}