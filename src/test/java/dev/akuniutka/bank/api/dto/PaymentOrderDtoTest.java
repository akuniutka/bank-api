package dev.akuniutka.bank.api.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.Amount.*;

class PaymentOrderDtoTest {
    @Test
    void testGetUserId() {
        PaymentOrderDto order = new PaymentOrderDto();
        assertNull(order.getUserId());
    }

    @Test
    void setUserId() {
        Long expected = 1L;
        PaymentOrderDto order = new PaymentOrderDto();
        order.setUserId(expected);
        assertEquals(expected, order.getUserId());
    }

    @Test
    void testGetReceiverId() {
        PaymentOrderDto order = new PaymentOrderDto();
        assertNull(order.getReceiverId());
    }

    @Test
    void setReceiverId() {
        Long expected = 1L;
        PaymentOrderDto order = new PaymentOrderDto();
        order.setReceiverId(expected);
        assertEquals(expected, order.getReceiverId());
    }

    @Test
    void getAmount() {
        PaymentOrderDto order = new PaymentOrderDto();
        assertNull(order.getAmount());
    }

    @Test
    void setAmount() {
        BigDecimal expected = TEN;
        PaymentOrderDto order = new PaymentOrderDto();
        order.setAmount(expected);
        assertEquals(expected, order.getAmount());
    }
}