package com.gerze.order_service.domain.repository;

import com.gerze.order_service.domain.model.product.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> findById(String id);

    Mono<Product> save(Product product);

    Mono<Void> deleteById(String id);

    Flux<Product> findAll(int page, int size);
}
