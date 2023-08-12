package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.exception.BadRequestException;
import dev.akuniutka.bank.api.exception.UserNotFoundException;
import dev.akuniutka.bank.api.exception.UserNotFoundToGetBalanceException;
import dev.akuniutka.bank.api.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class GeneralApiExceptionHandlerTest {
    private static final String GET_BALANCE = "/getBalance";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String USER_ID_IS_NULL = "user id is null";
    private static final String USER_NOT_FOUND = "user not found";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private AccountService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void catchUserNotFoundToGetBalanceException() throws Exception {
        ResponseDto response = new ResponseDto(BigDecimal.ONE.negate(), USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        given(service.getUserBalance(1L)).willThrow(new UserNotFoundToGetBalanceException(USER_NOT_FOUND));
        mvc.perform(get(GET_BALANCE + "/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchUserNotFoundExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        order.setUserId(0L);
        order.setAmount(BigDecimal.TEN);
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(service).increaseUserBalance(0L, BigDecimal.TEN);
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
        order.setUserId(0L);
        order.setAmount(BigDecimal.ONE);
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_NOT_FOUND);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new UserNotFoundException(USER_NOT_FOUND)).when(service).decreaseUserBalance(0L, BigDecimal.ONE);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }

    @Test
    void catchBadRequestExceptionWhenPutMoney() throws Exception {
        CashOrderDto order = new CashOrderDto();
        String jsonOrder = objectMapper.writeValueAsString(order);
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(service).increaseUserBalance(null,null);
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
        ResponseDto response = new ResponseDto(BigDecimal.ZERO, USER_ID_IS_NULL);
        String expected = objectMapper.writeValueAsString(response);
        doThrow(new BadRequestException(USER_ID_IS_NULL)).when(service).decreaseUserBalance(null,null);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
    }
}