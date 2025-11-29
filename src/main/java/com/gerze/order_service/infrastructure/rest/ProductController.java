package com.gerze.order_service.infrastructure.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.gerze.order_service.application.command.CreateProductCommand;
import com.gerze.order_service.application.command.UpdateProductCommand;
import com.gerze.order_service.application.dto.ProductDto;
import com.gerze.order_service.application.query.PageRequest;
import com.gerze.order_service.application.usecase.CreateProductUseCase;
import com.gerze.order_service.application.usecase.DeleteProductUseCase;
import com.gerze.order_service.application.usecase.GetProductUseCase;
import com.gerze.order_service.application.usecase.ListProductsUseCase;
import com.gerze.order_service.application.usecase.UpdateProductUseCase;
import com.gerze.order_service.domain.spec.SortedBy;
import com.gerze.order_service.infrastructure.rest.dto.ProductRequests.CreateProductRequest;
import com.gerze.order_service.infrastructure.rest.dto.ProductRequests.UpdateProductRequest;

import jakarta.validation.Valid;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final CreateProductUseCase createProduct;
    private final UpdateProductUseCase updateProduct;
    private final DeleteProductUseCase deleteProduct;
    private final GetProductUseCase getProduct;
    private final ListProductsUseCase listProducts;

    public ProductController(
        CreateProductUseCase createProduct,
        UpdateProductUseCase updateProduct,
        DeleteProductUseCase deleteProduct,
        GetProductUseCase getProduct,
        ListProductsUseCase listProducts
    ) {
        this.createProduct = createProduct;
        this.updateProduct = updateProduct;
        this.deleteProduct = deleteProduct;
        this.getProduct = getProduct;
        this.listProducts = listProducts;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductDto> create(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
            UUID.randomUUID(),
            request.name(),
            request.price(),
            request.description(),
            request.isActive() != null ? request.isActive() : Boolean.TRUE
        );
        return createProduct.create(command);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ProductDto> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProductRequest request
    ) {
        UpdateProductCommand command = new UpdateProductCommand(
            id,
            request.name(),
            request.price(),
            request.description(),
            request.isActive()
        );
        return updateProduct.update(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return deleteProduct.delete(id);
    }

    @GetMapping("/{id}")
    public Mono<ProductDto> get(@PathVariable UUID id) {
        return getProduct.get(id);
    }

    @GetMapping
    public Flux<ProductDto> list(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
        @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir
    ) {
        SortedBy sortedBy = new SortedBy(sortField, parseDirection(sortDir));
        PageRequest pageRequest = new PageRequest(page, size, sortedBy);
        return listProducts.list(pageRequest);
    }

    private org.springframework.data.domain.Sort.Direction parseDirection(String dir) {
        return "ASC".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
    }
}
