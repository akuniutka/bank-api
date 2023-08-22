package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.service.BalanceService;
import dev.akuniutka.bank.api.service.OperationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.Amount.*;

@WebMvcTest({BalanceController.class, OperationController.class})
class GeneralApiExceptionHandlerTest {
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String GET_OPERATIONS = "/getOperationList/{userId}";
    private static final Long USER_ID = 1L;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BalanceService balanceService;
    @MockBean
    private OperationService operationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void catchUserNotFoundToGetBalanceException() throws Exception {
        ResponseDto response = new ResponseDto(MINUS_ONE, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        given(balanceService.getUserBalance(USER_ID)).willThrow(new UserNotFoundToGetBalanceException(USER_NOT_FOUND));
        mvc.perform(get(GET_BALANCE, USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchUserNotFoundExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(TEN);
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(balanceService).increaseUserBalance(USER_ID, TEN);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchUserNotFoundExceptionWhenTakeMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(ONE);
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(balanceService).decreaseUserBalance(USER_ID, ONE);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchUserNotFoundExceptionWhenGetOperationList() throws Exception {
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        given(operationService.getOperations(USER_ID, null, null))
                .willThrow(new UserNotFoundException(USER_NOT_FOUND));
        mvc.perform(get(GET_OPERATIONS, USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchBadRequestExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(balanceService).increaseUserBalance(null, NULL);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchBadRequestExceptionWhenTakeMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(balanceService).decreaseUserBalance(null, NULL);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }
}