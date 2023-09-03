package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.dto.PaymentOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.entity.Account;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;
import static dev.akuniutka.bank.api.util.DateChecker.isDateBetween;
import static dev.akuniutka.bank.api.util.WebTestClientWrapper.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiControllerIT {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ZoneOffset OFFSET = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String TRANSFER_MONEY = "/transferMoney";
    private static final String GET_OPERATIONS = "/getOperationList/{userId}?dateFrom={dateFrom}&dateTo={dateTo}";
    private static final List<OperationDto> DTO_LIST = new ArrayList<>();
    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void init() {
        OBJECT_MAPPER.findAndRegisterModules();
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OffsetDateTime date = OffsetDateTime.of(LocalDate.parse("2023-01-01"), LocalTime.MIDNIGHT, OFFSET);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
        date = date.withOffsetSameInstant(offset);
        Account account = new Account();
        for (int i = 0; i < 12; i++) {
            Operation operation;
            if (i == 0 || i == 11) {
                operation = new Operation(account, OperationType.DEPOSIT, TEN, date);
            } else {
                operation = new Operation(account, OperationType.WITHDRAWAL, ONE, date);
            }
            date = date.plusMonths(1L);
            DTO_LIST.add(new OperationDto(operation));
        }
    }

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
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
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
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, PUT_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN_THOUSANDTHS);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
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
        CashOrderDto order = cashOrderFrom(userId, null);
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
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
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
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_ZERO);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
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
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TAKE_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
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
        CashOrderDto order = cashOrderFrom(userId, null);
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

    @Test
    void testTransferMoneyWhenUserIdIsNull() throws Exception {
        Long receiverId = 1073L;
        PaymentOrderDto order = paymentOrderFrom(null, receiverId, TEN);
        String expected = jsonResponseFrom(ZERO, USER_ID_IS_NULL);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenReceiverIdIsNull() throws Exception {
        Long userId = 1074L;
        PaymentOrderDto order = paymentOrderFrom(userId, null, TEN);
        String expected = jsonResponseFrom(ZERO, RECEIVER_ID_IS_NULL);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenUserNotFound() throws Exception {
        Long userId = 0L;
        Long receiverId = 1075L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN);
        String expected = jsonResponseFrom(ZERO, USER_NOT_FOUND);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenReceiverNotFound() throws Exception {
        Long userId = 1076L;
        Long receiverId = 0L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN);
        String expected = jsonResponseFrom(ZERO, RECEIVER_NOT_FOUND);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenAmountIsNull() throws Exception {
        Long userId = 1077L;
        Long receiverId = 1078L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, null);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NULL);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenAmountIsNegative() throws Exception {
        Long userId = 1079L;
        Long receiverId = 1080L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, MINUS_TEN);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_NEGATIVE);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenAmountIsZero() throws Exception {
        Long userId = 1081L;
        Long receiverId = 1082L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, ZERO);
        String expected = jsonResponseFrom(ZERO, AMOUNT_IS_ZERO);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThanTwoAndWithNonZeros() throws Exception {
        Long userId = 1083L;
        Long receiverId = 1084L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, ONE_THOUSANDTH);
        String expected = jsonResponseFrom(ZERO, WRONG_MINOR_UNITS);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenBalanceIsNotSufficient() throws Exception {
        Long userId = 1085L;
        Long receiverId = 1086L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN);
        String expected = jsonResponseFrom(ZERO, INSUFFICIENT_BALANCE);
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testTransferMoneyWhenAmountIsLessThanBalance() throws Exception {
        Long userId = 1087L;
        Long receiverId = 1088L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN);
        String expected = jsonResponseFrom(ONE);
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_BALANCE, receiverId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.OUTGOING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
        get(webTestClient, GET_OPERATIONS, receiverId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.INCOMING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testTransferMoneyWhenAmountIsEqualToBalance() throws Exception {
        Long userId = 1089L;
        Long receiverId = 1090L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN);
        String expected = jsonResponseFrom(ONE);
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_ZERO);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, receiverId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.OUTGOING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
        get(webTestClient, GET_OPERATIONS, receiverId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.INCOMING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN.setScale(1, RoundingMode.HALF_UP))
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testTransferMoneyWhenScaleIsGreaterThanTwoButWithZeros() throws Exception {
        Long userId = 1091L;
        Long receiverId = 1092L;
        PaymentOrderDto order = paymentOrderFrom(userId, receiverId, TEN_THOUSANDTHS);
        String expected = jsonResponseFrom(ONE);
        OffsetDateTime start = OffsetDateTime.now();
        put(webTestClient, TRANSFER_MONEY, order)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        OffsetDateTime finish = OffsetDateTime.now();
        expected = jsonResponseFrom(FORMATTED_TEN);
        get(webTestClient, GET_BALANCE, userId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        expected = jsonResponseFrom(FORMATTED_TEN_THOUSANDTHS);
        get(webTestClient, GET_BALANCE, receiverId)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
        get(webTestClient, GET_OPERATIONS, userId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.OUTGOING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN_THOUSANDTHS)
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
        get(webTestClient, GET_OPERATIONS, receiverId, start.toLocalDate(), finish.toLocalDate().plusDays(1L))
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].length()").isEqualTo(3)
                .jsonPath("$[0].type").isEqualTo(OperationType.INCOMING_TRANSFER.getDescription())
                .jsonPath("$[0].amount").isEqualTo(FORMATTED_TEN_THOUSANDTHS)
                .jsonPath("$[0].date").value(d -> isDateBetween(d, start, finish));
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNull() throws Exception {
        Long userId = 1070L;
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        get(webTestClient, GET_OPERATIONS, userId, null, null)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNull() throws Exception {
        Long userId = 1070L;
        List<OperationDto> dtoList = new ArrayList<>(DTO_LIST);
        dtoList.remove(0);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        get(webTestClient, GET_OPERATIONS, userId, "2023-02-01", null)
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNotNull() throws Exception {
        Long userId = 1070L;
        List<OperationDto> dtoList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            dtoList.add(DTO_LIST.get(i));
        }
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        get(webTestClient, GET_OPERATIONS, userId, null, "2023-07-01")
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNotNull() throws Exception {
        Long userId = 1070L;
        List<OperationDto> dtoList = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            dtoList.add(DTO_LIST.get(i));
        }
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        get(webTestClient, GET_OPERATIONS, userId, "2023-02-01", "2023-07-01")
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenUserDoesNotExist() throws Exception {
        Long userId = 0L;
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        get(webTestClient, GET_OPERATIONS, userId, null, null)
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenNoOperationsFound() throws Exception {
        Long userId = 1070L;
        ResponseDto response = new ResponseDto(ZERO, OPERATIONS_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        get(webTestClient, GET_OPERATIONS, userId, "2022-01-01", "2021-01-01")
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    private CashOrderDto cashOrderFrom(Long userId, BigDecimal amount) {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(userId);
        order.setAmount(amount);
        return order;
    }

    private PaymentOrderDto paymentOrderFrom(Long userId, Long receiverId, BigDecimal amount) {
        PaymentOrderDto order = new PaymentOrderDto();
        order.setUserId(userId);
        order.setReceiverId(receiverId);
        order.setAmount(amount);
        return order;
    }

    private String jsonResponseFrom(BigDecimal result, String message) throws Exception {
        ResponseDto response = new ResponseDto(result, message);
        return OBJECT_MAPPER.writeValueAsString(response);
    }

    private String jsonResponseFrom(BigDecimal result) throws Exception {
        ResponseDto response = new ResponseDto(result);
        return OBJECT_MAPPER.writeValueAsString(response);
    }
}