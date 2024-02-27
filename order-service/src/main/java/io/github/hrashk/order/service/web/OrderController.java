package io.github.hrashk.order.service.web;

import io.github.hrashk.order.service.broker.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    @Value("${app.kafka.order.topic}")
    private String topicName;

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @PostMapping
    public ResponseEntity<OrderResponse> sendOrderEvent(@RequestBody OrderEvent event) {
        kafkaTemplate.send(topicName, event);
        
        return ResponseEntity.ok(new OrderResponse("Message sent"));
    }
}
