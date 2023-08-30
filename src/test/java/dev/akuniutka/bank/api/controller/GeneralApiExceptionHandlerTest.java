package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.PaymentOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.service.AccountService;
import dev.akuniutka.bank.api.service.ApiService;
import dev.akuniutka.bank.api.service.TransferService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static dev.akuniutka.bank.api.util.ErrorMessage.*;
import static dev.akuniutka.bank.api.util.Amount.*;

@WebMvcTest({ApiController.class})
class GeneralApiExceptionHandlerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Long USER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String TRANSFER_MONEY = "/transferMoney";
    private static final String GET_OPERATIONS = "/getOperationList/{userId}";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ApiService apiService;
    @MockBean
    private AccountService accountService;
    @MockBean
    private TransferService transferService;

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(apiService));
        verifyNoMoreInteractions(ignoreStubs(accountService));
        verifyNoMoreInteractions(ignoreStubs(transferService));
    }

    @Test
    void catchUserNotFoundToGetBalanceException() throws Exception {
        ResponseDto response = new ResponseDto(MINUS_ONE, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        given(accountService.getUserBalance(USER_ID)).willThrow(new UserNotFoundToGetBalanceException(USER_NOT_FOUND));
        mvc.perform(get(GET_BALANCE, USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(accountService).getUserBalance(USER_ID);
    }

    @Test
    void catchUserNotFoundExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(TEN);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(apiService).putMoney(USER_ID, TEN);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(apiService).putMoney(USER_ID, TEN);
    }

    @Test
    void catchUserNotFoundExceptionWhenTakeMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(ONE);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(apiService).takeMoney(USER_ID, ONE);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(apiService).takeMoney(USER_ID, ONE);
    }

    @Test
    void catchUserNotFoundExceptionWhenTransferMoney() throws Exception {
        PaymentOrderDto order = new PaymentOrderDto();
        order.setUserId(USER_ID);
        order.setReceiverId(RECEIVER_ID);
        order.setAmount(TEN);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        when(transferService.createTransfer(USER_ID, RECEIVER_ID, TEN))
                .thenThrow(new UserNotFoundException(USER_NOT_FOUND));
        mvc.perform(put(TRANSFER_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(transferService).createTransfer(USER_ID, RECEIVER_ID, TEN);
    }

    @Test
    void catchUserNotFoundExceptionWhenGetOperationList() throws Exception {
        ResponseDto response = new ResponseDto(ZERO, USER_NOT_FOUND);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        given(apiService.getOperationList(USER_ID, null, null))
                .willThrow(new UserNotFoundException(USER_NOT_FOUND));
        mvc.perform(get(GET_OPERATIONS, USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(apiService).getOperationList(USER_ID, null, null);
    }

    @Test
    void catchBadRequestExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_ID_IS_NULL);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(apiService).putMoney(null, NULL);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(apiService).putMoney(null, NULL);
    }

    @Test
    void catchBadRequestExceptionWhenTakeMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_ID_IS_NULL);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(apiService).takeMoney(null, NULL);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(apiService).takeMoney(null, NULL);
    }

    @Test
    void catchBadRequestExceptionWhenTransferMoney() throws Exception {
        PaymentOrderDto order = new PaymentOrderDto();
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        ResponseDto response = new ResponseDto(ZERO, USER_ID_IS_NULL);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        when(transferService.createTransfer(null, null, null))
                .thenThrow(new BadRequestException(USER_ID_IS_NULL));
        mvc.perform(put(TRANSFER_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(transferService).createTransfer(null, null, null);
    }
}