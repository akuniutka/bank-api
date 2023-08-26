package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.akuniutka.bank.api.dto.CashOrderDto;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.dto.PaymentOrderDto;
import dev.akuniutka.bank.api.dto.ResponseDto;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.service.ApiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static dev.akuniutka.bank.api.util.Amount.*;

@WebMvcTest(ApiController.class)
class ApiControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Long USER_ID = 1L;
    private static final Long RECEIVER_ID = 2L;
    private static final String GET_BALANCE = "/getBalance/{userId}";
    private static final String PUT_MONEY = "/putMoney";
    private static final String TAKE_MONEY = "/takeMoney";
    private static final String TRANSFER_MONEY = "/transferMoney";
    private static final String GET_OPERATION_LIST = "/getOperationList/{userId}?dateFrom={dateFrom}&dateTo={dateTo}";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ApiService service;

    @BeforeAll
    static void init() {
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @AfterEach
    public void tearDown() {
        verifyNoMoreInteractions(ignoreStubs(service));
    }

    @Test
    void testApiController() {
        assertDoesNotThrow(() -> new ApiController(null));
    }

    @Test
    void testGetBalance() throws Exception {
        BigDecimal balance = FORMATTED_TEN;
        ResponseDto response = new ResponseDto(balance);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        when(service.getBalance(USER_ID)).thenReturn(balance);
        mvc.perform(get(GET_BALANCE, USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).getBalance(USER_ID);
    }

    @Test
    void testPutMoney() throws Exception {
        ResponseDto response = new ResponseDto(ONE);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(TEN);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        doNothing().when(service).putMoney(USER_ID, TEN);
        mvc.perform(put(PUT_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).putMoney(USER_ID, TEN);
    }

    @Test
    void testTakeMoney() throws Exception {
        ResponseDto response = new ResponseDto(ONE);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        CashOrderDto order = new CashOrderDto();
        order.setUserId(USER_ID);
        order.setAmount(ONE);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        doNothing().when(service).takeMoney(USER_ID, ONE);
        mvc.perform(put(TAKE_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).takeMoney(USER_ID, ONE);
    }

    @Test
    void testTransferMoney() throws Exception {
        ResponseDto response = new ResponseDto(ONE);
        String expected = OBJECT_MAPPER.writeValueAsString(response);
        PaymentOrderDto order = new PaymentOrderDto();
        order.setUserId(USER_ID);
        order.setReceiverId(RECEIVER_ID);
        order.setAmount(TEN);
        String jsonOrder = OBJECT_MAPPER.writeValueAsString(order);
        doNothing().when(service).transferMoney(USER_ID, RECEIVER_ID, TEN);
        mvc.perform(put(TRANSFER_MONEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonOrder))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).transferMoney(USER_ID, RECEIVER_ID, TEN);
    }


    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNull() throws Exception {
        List<Operation> operations = generateTestOperationList();
        List<OperationDto> dtoList = generateDtoListFromOperationList(operations);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        when(service.getOperationList(USER_ID, null, null)).thenReturn(operations);
        mvc.perform(get(GET_OPERATION_LIST, USER_ID, null, null))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).getOperationList(USER_ID, null, null);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNull() throws Exception {
        LocalDate dateFromForQuery = LocalDate.of(2022, Month.JANUARY, 1);
        Date dateFrom = Date.from(dateFromForQuery.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Operation> operations = generateTestOperationList();
        List<OperationDto> dtoList = generateDtoListFromOperationList(operations);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        when(service.getOperationList(USER_ID, dateFrom, null)).thenReturn(operations);
        mvc.perform(get(GET_OPERATION_LIST, USER_ID, dateFromForQuery, null))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).getOperationList(USER_ID, dateFrom, null);
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNotNull() throws Exception {
        LocalDate dateToForQuery = LocalDate.of(2022, Month.JANUARY, 31);
        Date dateTo = Date.from(dateToForQuery.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Operation> operations = generateTestOperationList();
        List<OperationDto> dtoList = generateDtoListFromOperationList(operations);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        when(service.getOperationList(USER_ID, null, dateTo)).thenReturn(operations);
        mvc.perform(get(GET_OPERATION_LIST, USER_ID, null, dateToForQuery))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).getOperationList(USER_ID, null, dateTo);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNotNull() throws Exception {
        LocalDate dateFromForQuery = LocalDate.of(2022, Month.JANUARY, 1);
        Date dateFrom = Date.from(dateFromForQuery.atStartOfDay(ZoneId.systemDefault()).toInstant());
        LocalDate dateToForQuery = LocalDate.of(2022, Month.JANUARY, 31);
        Date dateTo = Date.from(dateToForQuery.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Operation> operations = generateTestOperationList();
        List<OperationDto> dtoList = generateDtoListFromOperationList(operations);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        when(service.getOperationList(USER_ID, dateFrom, dateTo)).thenReturn(operations);
        mvc.perform(get(GET_OPERATION_LIST, USER_ID, dateFromForQuery, dateToForQuery))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(service).getOperationList(USER_ID, dateFrom, dateTo);
    }

    private List<Operation> generateTestOperationList() {
        List<Operation> operations = new ArrayList<>();
        operations.add(new Operation());
        operations.get(0).setType(OperationType.DEPOSIT);
        operations.get(0).setAmount(TEN);
        operations.get(0).setDate(new Date(0L));
        operations.add(new Operation());
        operations.get(1).setType(OperationType.WITHDRAWAL);
        operations.get(1).setAmount(ONE);
        operations.get(1).setDate(new Date(1L));
        return operations;
    }

    private List<OperationDto> generateDtoListFromOperationList(List<Operation> operations) {
        List<OperationDto> dtoList = new ArrayList<>();
        for (Operation operation : operations) {
            dtoList.add(new OperationDto(operation));
        }
        return dtoList;
    }
}