package com.gerze.order_service.application.usecase;

import java.util.UUID;

import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface GetOrderUseCase {
    Mono<OrderDto> get(UUID orderId);
}
