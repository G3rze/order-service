package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.RemoveItemCommand;
import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface RemoveItemUseCase {
    Mono<OrderDto> remove(RemoveItemCommand command);
}
