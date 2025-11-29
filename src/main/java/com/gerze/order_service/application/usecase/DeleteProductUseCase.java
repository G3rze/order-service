package com.gerze.order_service.application.usecase;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface DeleteProductUseCase {
    Mono<Void> delete(UUID productId);
}
