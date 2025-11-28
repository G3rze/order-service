package com.gerze.order_service.domain.repository;

import com.gerze.order_service.domain.model.order.Order;
import com.gerze.order_service.domain.spec.filter.OrderFilter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository {
    Mono<Order> save(Order order);

    Mono<Order> findById(String id);

    Flux<Order> findAll(OrderFilter filter, int page, int size);
}
