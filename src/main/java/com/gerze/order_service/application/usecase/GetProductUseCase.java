package com.gerze.order_service.application.usecase;

import java.util.UUID;

import com.gerze.order_service.application.dto.ProductDto;

import reactor.core.publisher.Mono;

public interface GetProductUseCase {
    Mono<ProductDto> get(UUID productId);
}
