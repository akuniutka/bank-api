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
        CashOrderDto orderDto = new CashOrderDto(expected, BigDecimal.TEN);
        assertEquals(expected, orderDto.getUserId());
    }

    @Test
    void testGetAmount() {
        BigDecimal expected = BigDecimal.TEN;
        CashOrderDto orderDto = new CashOrderDto(1L, expected);
        assertEquals(expected, orderDto.getAmount());
    }
}