package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.UpdateQuantityCommand;
import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface UpdateItemQuantityUseCase {
    Mono<OrderDto> update(UpdateQuantityCommand command);
}
