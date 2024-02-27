package io.github.hrashk.order.service.broker;

public record OrderEvent(String product, Integer quantity) {
}
