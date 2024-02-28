package io.github.hrashk.order.service;

import io.github.hrashk.order.service.broker.OrderEvent;
import io.github.hrashk.order.service.broker.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = KafkaInitializer.class)
@Import({KafkaTestListener.class, KafkaTestConfiguration.class})
class OrderStatusServiceApplicationTests {
    @Autowired
    private KafkaTestListener listener;

    @Value("${app.kafka.order.topic}")
    private String orderTopic;

    @Autowired
    private KafkaTemplate<String, OrderEvent> eventTemplate;

    @Test
    void receiveThenSend() {
        eventTemplate.send(orderTopic, new OrderEvent("Eiderdown", 13));

        await()
                .pollInterval(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(10))
                .until(listener::hasStatuses);

        OrderStatus status = listener.getLastStatus();

        assertThat(status.status()).isNotBlank();
        assertThat(status.date()).isNotNull();
    }
}
