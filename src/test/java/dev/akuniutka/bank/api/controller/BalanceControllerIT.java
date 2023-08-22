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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static dev.akuniutka.bank.api.entity.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BalanceControllerIT {
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String GET_OPERATIONS = "/getOperationList/{userId}?dateFrom={dateFrom}&dateTo={dateTo}";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat DATE_FROM_JSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetBalanceWhenUserExists() throws Exception {
        Long userId = 1053L;
        String expected = jsonResponseFrom(FORMATTED_TEN);
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
        String expected = jsonResponseFrom(MINUS_ONE, USER_NOT_FOUND);
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
        Long userId = 1054L;
        CashOrderDto order = cashOrderFrom(userId, TEN);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
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
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        webTestClient
                .get()
                .uri(GET_OPERATIONS, userId, dateFrom, dateTo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
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
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN_THOUSANDTHS);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        webTestClient
                .get()
                .uri(GET_OPERATIONS, userId, dateFrom, dateTo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
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
        Long userId = 1057L;
        CashOrderDto order = cashOrderFrom(userId, ZERO);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_ZERO);
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
        Long userId = 1058L;
        CashOrderDto order = cashOrderFrom(userId, MINUS_TEN);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NEGATIVE);
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
        Long userId = 1059L;
        CashOrderDto order = cashOrderFrom(userId, NULL);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NULL);
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
        CashOrderDto order = cashOrderFrom(userId, TEN);
        String expected = jsonResponseFrom(ZERO, USER_NOT_FOUND);
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
        CashOrderDto order = cashOrderFrom(null, TEN);
        String expected = jsonResponseFrom(ZERO, USER_ID_IS_NULL);
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
        Long userId = 1060L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ONE);
        Date start = new Date();
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
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        webTestClient
                .get()
                .uri(GET_OPERATIONS, userId, dateFrom, dateTo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
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
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_ZERO);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        webTestClient
                .get()
                .uri(GET_OPERATIONS, userId, dateFrom, dateTo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
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
        Date finish = new Date();
        expected = jsonResponseFrom(FORMATTED_TEN);
        webTestClient
                .get()
                .uri(GET_BALANCE, userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        LocalDate dateFrom = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateTo = finish.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1L);
        webTestClient
                .get()
                .uri(GET_OPERATIONS, userId, dateFrom, dateTo)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
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
        Long userId = 1064L;
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ZERO, INSUFFICIENT_BALANCE);
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
        Long userId = 1065L;
        CashOrderDto order = cashOrderFrom(userId, ZERO);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_ZERO);
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
        Long userId = 1066L;
        CashOrderDto order = cashOrderFrom(userId, MINUS_ONE);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NEGATIVE);
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
        Long userId = 1067L;
        CashOrderDto order = cashOrderFrom(userId, NULL);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NULL);
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
        CashOrderDto order = cashOrderFrom(userId, ONE);
        String expected = jsonResponseFrom(ZERO, USER_NOT_FOUND);
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
        CashOrderDto order = cashOrderFrom(null, ONE);
        String expected = jsonResponseFrom(ZERO, USER_ID_IS_NULL);
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

    private void isDateBetween(Object o, Date start, Date finish) {
        try {
            Date date = DATE_FROM_JSON.parse((String) o);
            if (date == null || start == null || finish == null
                    || start.compareTo(date) * date.compareTo(finish) < 0
            ) {
                throw new IllegalArgumentException("not between");
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException("wrong date format");
        }
    }
}