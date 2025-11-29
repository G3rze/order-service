package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.UpdateProductCommand;
import com.gerze.order_service.application.dto.ProductDto;

import reactor.core.publisher.Mono;

public interface UpdateProductUseCase {
    Mono<ProductDto> update(UpdateProductCommand command);
}
