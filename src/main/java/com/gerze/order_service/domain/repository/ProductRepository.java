package com.gerze.order_service.domain.repository;

import java.util.UUID;

import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.spec.SortedBy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {
    Mono<Product> findById(UUID id);

    Mono<Product> save(Product product);

    Mono<Void> deleteById(UUID id);

    Flux<Product> findAll(int page, int size, SortedBy sortedBy);
}
