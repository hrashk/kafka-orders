package io.github.hrashk.order.service.broker;

import java.time.Instant;

public record OrderStatus(String status, Instant date) {
}
