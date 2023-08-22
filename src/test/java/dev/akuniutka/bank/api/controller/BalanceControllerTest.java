package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.service.BalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static dev.akuniutka.bank.api.util.Amount.*;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private BalanceService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testBalanceController() {
        assertDoesNotThrow(() -> new BalanceController(null));
    }

    @Test
    void testGetBalance() throws Exception {
        BigDecimal balance = FORMATTED_TEN;
        ResponseDto response = new ResponseDto(balance);
        String expected = objectMapper.writeValueAsString(response);
        when(service.getUserBalance(USER_ID)).thenReturn(balance);
        mvc.perform(get(GET_BALANCE, USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service, times(MAX_MOCK_CALLS)).getUserBalance(USER_ID);
        verifyNoMoreInteractions(ignoreStubs(service));
    }

    @Test
    void testPutMoney() throws Exception {
        ResponseDto response = new ResponseDto(ONE);
        String expected = objectMapper.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(TEN);
        String jsonOrder = objectMapper.writeValueAsString(order);
        doNothing().when(service).increaseUserBalance(USER_ID, TEN);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service, times(MAX_MOCK_CALLS)).increaseUserBalance(USER_ID, TEN);
        verifyNoMoreInteractions(ignoreStubs(service));
    }

    @Test
    void testTakeMoney() throws Exception {
        ResponseDto response = new ResponseDto(ONE);
        String expected = objectMapper.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(ONE);
        String jsonOrder = objectMapper.writeValueAsString(order);
        doNothing().when(service).decreaseUserBalance(USER_ID, ONE);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service, times(MAX_MOCK_CALLS)).decreaseUserBalance(USER_ID, ONE);
        verifyNoMoreInteractions(ignoreStubs(service));
    }
}