package com.gerze.order_service.domain.repository;

import java.util.UUID;

import com.gerze.order_service.domain.model.order.OrderStatus;

import reactor.core.publisher.Mono;

public interface OrderStatusRepository {
    Mono<OrderStatus> findById(UUID id);
}
