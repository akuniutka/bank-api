package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    private static final String GET_BALANCE = "/getBalance";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private AccountService service;

    @Test
    void testAccountController() {
        assertDoesNotThrow(() -> new AccountController(null));
    }

    @Test
    void testGetBalance() throws Exception {
        BigDecimal balance = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
        given(service.getUserBalance(1L)).willReturn(balance);
        mvc.perform(get(GET_BALANCE + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":" + balance + ",\"message\":\"\"}"));
    }

    @Test
    void testPutMoney() throws Exception {
        String json = "{\"userId\":1,\"amount\":10}";
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":1,\"message\":\"\"}"));
    }

    @Test
    void testTakeMoney() throws Exception {
        String json = "{\"userId\":1,\"amount\":10}";
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"result\":1,\"message\":\"\"}"));
    }
}