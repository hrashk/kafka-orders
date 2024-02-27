package io.github.hrashk.order.service;

import io.github.hrashk.order.service.broker.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TestComponent
@Slf4j
public class KafkaTestListener {
    List<OrderEvent> orderEvents = new ArrayList<>();
    @KafkaListener(topics = "${app.kafka.order.topic}", groupId = "${app.kafka.order.group-id}",
            containerFactory = "concurrentKafkaListenerContainerFactory")
    public void listen(@Payload OrderEvent message,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {
        log.info("Received message {}", message);
        log.info("Key: {}, Topic: {}, Partition: {}, Timestamp: {}", key, topic, partition, timestamp);

        orderEvents.add(message);
    }

    public boolean hasEvents() {
        return !orderEvents.isEmpty();
    }

    public OrderEvent getLastEvent() {
        return orderEvents.get(orderEvents.size() - 1);
    }
}
