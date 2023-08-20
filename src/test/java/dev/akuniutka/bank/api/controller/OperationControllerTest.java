package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import dev.akuniutka.bank.api.service.Operations;
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
import static dev.akuniutka.bank.api.Amount.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationController.class)
class OperationControllerTest {
    private static final int MAX_MOCK_CALLS = 1;
    private static final Long USER_ID = 1L;
    private static final String GET_OPERATION_LIST = "/getOperationList";
    @Autowired
    private MockMvc mvc;
    @MockBean
    private Operations operations;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testOperationController() {
        assertDoesNotThrow(() -> new OperationController(null));
    }

    @Test
    void testGetOperationList() throws Exception {
        String query = "?userId=1&dateFrom=2022-01-01&dateTo=2022-01-31";
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2022, Calendar.JANUARY, 1);
        Date start = calendar.getTime();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        Date finish = calendar.getTime();
        List<OperationDto> dtoList = new ArrayList<>();
        Operation operation = new Operation();
        operation.setDate(start);
        operation.setType(OperationType.DEPOSIT);
        operation.setAmount(TEN);
        dtoList.add(new OperationDto(operation));
        operation.setDate(finish);
        operation.setType(OperationType.WITHDRAWAL);
        operation.setAmount(ONE);
        dtoList.add(new OperationDto(operation));
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String expected = objectMapper.writeValueAsString(dtoList);
        when(operations.getList(USER_ID, start, finish)).thenReturn(dtoList);
        mvc.perform(get(GET_OPERATION_LIST + query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expected, true));
        verify(operations, times(MAX_MOCK_CALLS)).getList(USER_ID, start, finish);
        verifyNoMoreInteractions(ignoreStubs(operations));
    }
}