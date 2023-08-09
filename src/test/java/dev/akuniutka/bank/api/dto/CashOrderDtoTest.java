package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CashOrderDtoTest {
    @Test
    void testCashOrderDto() {
        assertDoesNotThrow(() -> new CashOrderDto(1L, BigDecimal.TEN));
    }

    @Test
    void testGetUserId() {
        Long expected = 1L;
        CashOrderDto order = new CashOrderDto(expected, BigDecimal.TEN);
        assertEquals(expected, order.getUserId());
    }

    @Test
    void testGetAmount() {
        BigDecimal expected = BigDecimal.TEN;
        CashOrderDto order = new CashOrderDto(1L, expected);
        assertEquals(expected, order.getAmount());
    }
}