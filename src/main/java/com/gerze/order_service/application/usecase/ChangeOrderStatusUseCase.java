package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.ChangeStatusCommand;
import com.gerze.order_service.application.dto.OrderDto;

import reactor.core.publisher.Mono;

public interface ChangeOrderStatusUseCase {
    Mono<OrderDto> changeStatus(ChangeStatusCommand command);
}
