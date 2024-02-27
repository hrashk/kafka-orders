package io.github.hrashk.order.service;

import io.github.hrashk.order.service.broker.OrderEvent;
import io.github.hrashk.order.service.web.Order;
import io.github.hrashk.order.service.web.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = KafkaInitializer.class)
@Import(KafkaTestListener.class)
class OrderServiceApplicationTests {
    private static final String SERVICE_URL = "/api/v1/orders";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private KafkaTestListener listener;

    @Test
    void sendOrderEvent() {
        ResponseEntity<OrderResponse> response = rest.postForEntity(SERVICE_URL,
                new Order("some product", 13),
                OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().message()).isNotBlank();

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(10))
                .until(listener::hasEvents);

        OrderEvent event = listener.getLastEvent();

        assertThat(event.product()).isNotBlank();
        assertThat(event.quantity()).isPositive();
    }
}
