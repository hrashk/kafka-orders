package io.github.hrashk.order.service.broker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class OrderEventListener {
    @Autowired
    KafkaTemplate<String, OrderStatus> statusTemplate;

    @Value("${app.kafka.status.topic}")
    private String statusTopic;

    @KafkaListener(topics = "${app.kafka.order.topic}", groupId = "${app.kafka.status.group-id}",
            containerFactory = "eventListenerContainerFactory")
    public void listen(@Payload OrderEvent message,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
                       @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {

        statusTemplate.send(statusTopic, new OrderStatus("PROCESS", Instant.now()));
    }
}
