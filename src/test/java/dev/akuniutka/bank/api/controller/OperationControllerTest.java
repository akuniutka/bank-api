package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.service.OperationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static dev.akuniutka.bank.api.util.Amount.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationController.class)
class OperationControllerTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private static final String URI = "/getOperationList/{userId}";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private OperationService operationService;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<Operation> OPERATIONS = new ArrayList<>();
    private static final List<OperationDto> DTO_LIST = new ArrayList<>();
    private static Date dateFrom;
    private static Date dateTo;

    @BeforeAll
    static void init() {
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2022, Calendar.JANUARY, 1);
        dateFrom = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        dateTo = calendar.getTime();
        OPERATIONS.add(new Operation());
        OPERATIONS.add(new Operation());
        OPERATIONS.get(0).setDate(dateFrom);
        OPERATIONS.get(0).setType(OperationType.DEPOSIT);
        OPERATIONS.get(0).setAmount(TEN);
        OPERATIONS.get(1).setDate(dateTo);
        OPERATIONS.get(1).setType(OperationType.WITHDRAWAL);
        OPERATIONS.get(1).setAmount(ONE);
        DTO_LIST.add(new OperationDto(OPERATIONS.get(0)));
        DTO_LIST.add(new OperationDto(OPERATIONS.get(1)));
    }

    @Test
    void testOperationController() {
        assertDoesNotThrow(() -> new OperationController(null));
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNull() throws Exception {
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        when(operationService.getOperations0(USER_ID, null, null)).thenReturn(new ArrayList<>(OPERATIONS));
        mvc.perform(get(URI, USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(operationService, times(MAX_MOCK_CALLS)).getOperations0(USER_ID, null, null);
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNull() throws Exception {
        String uri = URI + "?dateFrom={dateFrom}";
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        when(operationService.getOperations0(USER_ID, dateFrom, null)).thenReturn(new ArrayList<>(OPERATIONS));
        mvc.perform(get(uri, USER_ID, "2022-01-01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(operationService, times(MAX_MOCK_CALLS)).getOperations0(USER_ID, dateFrom, null);
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNotNull() throws Exception {
        String uri = URI + "?dateTo={dateTo}";
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        when(operationService.getOperations0(USER_ID, null, dateTo)).thenReturn(new ArrayList<>(OPERATIONS));
        mvc.perform(get(uri, USER_ID, "2022-01-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(operationService, times(MAX_MOCK_CALLS)).getOperations0(USER_ID, null, dateTo);
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNotNull() throws Exception {
        String uri = URI + "?dateFrom={dateFrom}&dateTo={dateTo}";
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        when(operationService.getOperations0(USER_ID, dateFrom, dateTo)).thenReturn(new ArrayList<>(OPERATIONS));
        mvc.perform(get(uri, USER_ID, "2022-01-01", "2022-01-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(operationService, times(MAX_MOCK_CALLS)).getOperations0(USER_ID, dateFrom, dateTo);
        verifyNoMoreInteractions(ignoreStubs(operationService));
    }
}