package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.AddItemCommand;
import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface AddItemUseCase {
    Mono<OrderDto> add(AddItemCommand command);
}
