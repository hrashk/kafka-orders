package io.github.hrashk.order.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.hrashk.order.service.broker.OrderEvent;
import io.github.hrashk.order.service.broker.OrderStatus;
import io.github.hrashk.order.service.broker.StatusListener;
import io.github.hrashk.order.service.web.Order;
import io.github.hrashk.order.service.web.OrderResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = KafkaInitializer.class)
@Import({KafkaTestListener.class, KafkaTestConfiguration.class})
class OrderServiceApplicationTests {
    private static final String SERVICE_URL = "/api/v1/orders";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private KafkaTestListener listener;

    @Value("${app.kafka.status.topic}")
    private String statusTopic;

    @Autowired
    private KafkaTemplate<String, OrderStatus> statusTemplate;

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

    /**
     * Spying on the logger following the approach: https://stackoverflow.com/a/52229629
     */
    @Test
    void receiveStatusEvent() {
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        Logger logger = (Logger) LoggerFactory.getLogger(StatusListener.class);
        logger.addAppender(listAppender);

        statusTemplate.send(statusTopic, new OrderStatus("IN PROGRESS", Instant.now()));

        List<ILoggingEvent> logsList = listAppender.list;
        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(10))
                .until(() -> logsList.size() > 1);

        assertThat(logsList.get(0).getFormattedMessage()).contains("Received message: ");
        assertThat(logsList.get(0).getFormattedMessage()).contains("PROGRESS");
        assertThat(logsList.get(1).getFormattedMessage()).contains("Topic: " + statusTopic);
    }
}
