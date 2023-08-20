package dev.akuniutka.bank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.entity.Operation;
import dev.akuniutka.bank.api.entity.OperationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static dev.akuniutka.bank.api.Amount.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationControllerIT {
    private static final String URI = "/getOperationList/{userId}";
    private static final Long USER_ID = 1020L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final List<OperationDto> DTO_LIST = new ArrayList<>();
    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void init() {
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2023, Calendar.JANUARY, 1);
        for (int i = 0; i < 12; i++) {
            Operation operation = new Operation();
            if (i < 2) {
                operation.setType(OperationType.DEPOSIT);
                operation.setAmount(TEN);
            } else {
                operation.setType(OperationType.WITHDRAWAL);
                operation.setAmount(ONE);
            }
            operation.setDate(calendar.getTime());
            calendar.add(Calendar.MONTH, 1);
            DTO_LIST.add(new OperationDto(operation));
        }
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNull() throws Exception {
        String expected = OBJECT_MAPPER.writeValueAsString(DTO_LIST);
        webTestClient
                .get()
                .uri(URI, USER_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNull() throws Exception {
        String uri = URI + "?dateFrom={dateFrom}";
        List<OperationDto> dtoList = new ArrayList<>(DTO_LIST);
        dtoList.remove(0);
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        webTestClient
                .get()
                .uri(uri, USER_ID, "2023-02-01")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNullAndDateToIsNotNull() throws Exception {
        String uri = URI + "?dateTo={dateTo}";
        List<OperationDto> dtoList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            dtoList.add(DTO_LIST.get(i));
        }
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        webTestClient
                .get()
                .uri(uri, USER_ID, "2023-07-01")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }

    @Test
    void testGetOperationListWhenDateFromIsNotNullAndDateToIsNotNull() throws Exception {
        String uri = URI + "?dateFrom={dateFrom}&dateTo={dateTo}";
        List<OperationDto> dtoList = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            dtoList.add(DTO_LIST.get(i));
        }
        String expected = OBJECT_MAPPER.writeValueAsString(dtoList);
        webTestClient
                .get()
                .uri(uri, USER_ID, "2023-02-01", "2023-07-01")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, true);
    }
}
