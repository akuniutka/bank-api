package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class CashOrderDtoTest {
    @Test
    void testGetUserId() {
        CashOrderDto order = new CashOrderDto();
        assertNull(order.getUserId());
    }

    @Test
    void testSetUserId() {
        Long expected = 1L;
        CashOrderDto order = new CashOrderDto();
        order.setUserId(expected);
        assertEquals(expected, order.getUserId());
    }

    @Test
    void testGetAmount() {
        CashOrderDto order = new CashOrderDto();
        assertNull(order.getAmount());
    }

    @Test
    void setAmount() {
        BigDecimal amount = TEN;
        CashOrderDto order = new CashOrderDto();
        order.setAmount(amount);
        assertEquals(0, amount.compareTo(order.getAmount()));
    }
}