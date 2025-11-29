package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.command.CreateProductCommand;
import com.gerze.order_service.application.dto.ProductDto;

import reactor.core.publisher.Mono;

public interface CreateProductUseCase {
    Mono<ProductDto> create(CreateProductCommand command);
}
