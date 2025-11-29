package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.CreateOrderCommand;
import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface CreateOrderUseCase {
    Mono<OrderDto> create(CreateOrderCommand command);
}
