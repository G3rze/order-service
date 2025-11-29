package com.gerze.order_service.application.service;

import java.util.Objects;
import java.util.UUID;

import com.gerze.order_service.application.command.CreateProductCommand;
import com.gerze.order_service.application.command.UpdateProductCommand;
import com.gerze.order_service.application.dto.ProductDto;
import com.gerze.order_service.application.exception.NotFoundException;
import com.gerze.order_service.application.exception.ValidationException;
import com.gerze.order_service.application.mapper.ProductMapper;
import com.gerze.order_service.application.query.PageRequest;
import com.gerze.order_service.application.usecase.CreateProductUseCase;
import com.gerze.order_service.application.usecase.DeleteProductUseCase;
import com.gerze.order_service.application.usecase.GetProductUseCase;
import com.gerze.order_service.application.usecase.ListProductsUseCase;
import com.gerze.order_service.application.usecase.UpdateProductUseCase;
import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.repository.ProductRepository;
import com.gerze.order_service.domain.spec.SortedBy;

import jakarta.validation.Valid;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.validation.annotation.Validated;

@Validated
public class ProductApplicationService implements
    CreateProductUseCase,
    UpdateProductUseCase,
    DeleteProductUseCase,
    GetProductUseCase,
    ListProductsUseCase {

    private final ProductRepository productRepository;

    public ProductApplicationService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    @Override
    public Mono<ProductDto> create(@Valid CreateProductCommand command) {
        Objects.requireNonNull(command, "CreateProductCommand cannot be null");

        UUID id = command.id() != null ? command.id() : UUID.randomUUID();
        Product product = ProductMapper.fromCreate(
            id,
            command.name(),
            command.price(),
            command.description(),
            command.isActive()
        );

        return productRepository.save(product)
            .map(ProductMapper::toDto);
    }

    @Override
    public Mono<ProductDto> update(@Valid UpdateProductCommand command) {
        Objects.requireNonNull(command, "UpdateProductCommand cannot be null");

        return findProduct(command.id())
            .map(product -> applyUpdates(product, command))
            .flatMap(productRepository::save)
            .map(ProductMapper::toDto);
    }

    @Override
    public Mono<Void> delete(UUID productId) {
        Objects.requireNonNull(productId, "Product id cannot be null");
        return productRepository.deleteById(productId);
    }

    @Override
    public Mono<ProductDto> get(UUID productId) {
        Objects.requireNonNull(productId, "Product id cannot be null");
        return findProduct(productId).map(ProductMapper::toDto);
    }

    @Override
    public Flux<ProductDto> list(@Valid PageRequest pageRequest) {
        int page = pageRequest != null ? pageRequest.page() : 0;
        int size = pageRequest != null ? pageRequest.size() : 20;
        if (page < 0 || size <= 0) {
            throw new ValidationException("Page must be >= 0 and size must be > 0");
        }
        SortedBy sortedBy = pageRequest != null
            ? Objects.requireNonNull(pageRequest.sortedBy(), "sortedBy cannot be null")
            : new SortedBy("createdAt", org.springframework.data.domain.Sort.Direction.DESC);
        return productRepository.findAll(page, size, sortedBy)
            .map(ProductMapper::toDto);
    }

    private Mono<Product> findProduct(UUID id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("Product not found")));
    }

    private Product applyUpdates(Product product, UpdateProductCommand command) {
        Product updated = product;
        if (command.name() != null) {
            updated = updated.withName(command.name());
        }
        if (command.price() != null) {
            updated = ProductMapper.applyUpdatePrice(updated, command.price());
        }
        if (command.description() != null) {
            updated = updated.withDescription(command.description());
        }
        if (command.isActive() != null) {
            updated = updated.withIsActive(command.isActive());
        }
        return updated;
    }

}
