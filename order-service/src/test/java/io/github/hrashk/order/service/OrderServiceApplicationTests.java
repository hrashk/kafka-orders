package io.github.hrashk.order.service;

import io.github.hrashk.order.service.web.Order;
import io.github.hrashk.order.service.web.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {
    private static final String SERVICE_URL = "/api/v1/orders";

    @Autowired
    protected TestRestTemplate rest;

    @Test
    void sendOrderEvent() {
        ResponseEntity<OrderResponse> response = rest.postForEntity(SERVICE_URL,
                new Order("some product", 13),
                OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().message()).isNotBlank();
    }
}
