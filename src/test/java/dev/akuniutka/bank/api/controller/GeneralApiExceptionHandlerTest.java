package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.exception.CashOrderException;
import dev.akuniutka.bank.api.exception.GetBalanceException;
import dev.akuniutka.bank.api.service.AccountService;
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

@WebMvcTest(AccountController.class)
class GeneralApiExceptionHandlerTest {
    private static final String GET_BALANCE = "/getBalance";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private AccountService service;

    @Test
    void catchGetBalanceException() throws Exception {
        given(service.getUserBalance(1L)).willThrow(new GetBalanceException("user not found"));
        mvc.perform(get(GET_BALANCE + "/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":-1,\"message\":\"user not found\"}"));
    }

    @Test
    void catchCashOrderExceptionWhenPutMoney() throws Exception {
        String json = "{\"userId\":null,\"amount\":null}";
        doThrow(new CashOrderException("user id is null")).when(service).increaseUserBalance(null,null);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":0,\"message\":\"user id is null\"}"));
    }

    @Test
    void catchCashOrderExceptionWhenTakeMoney() throws Exception {
        String json = "{\"userId\":null,\"amount\":null}";
        doThrow(new CashOrderException("user id is null")).when(service).decreaseUserBalance(null,null);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":0,\"message\":\"user id is null\"}"));
    }
}