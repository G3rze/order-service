package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.dto.ProductDto;
import com.gerze.order_service.application.query.PageRequest;

import reactor.core.publisher.Flux;

public interface ListProductsUseCase {
    Flux<ProductDto> list(PageRequest pageRequest);
}
