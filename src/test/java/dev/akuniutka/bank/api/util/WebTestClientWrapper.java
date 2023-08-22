package dev.akuniutka.bank.api.util;

import dev.akuniutka.bank.api.dto.CashOrderDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public class WebTestClientWrapper {
    public static WebTestClient.ResponseSpec get(WebTestClient webTestClient, String uri, Object... uriVariables) {
        return webTestClient
                .get()
                .uri(uri, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .exchange();
    }

    public static WebTestClient.ResponseSpec put(WebTestClient webTestClient, String uri, CashOrderDto order) {
        return webTestClient
                .put()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(order)
                .exchange();
    }
}
