package com.gerze.order_service.application.mapper;

import com.gerze.order_service.application.dto.ProductDto;
import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.model.shared.Money;

public class ProductMapper {

    private ProductMapper() {
    }

    public static ProductDto toDto(Product product) {
        return new ProductDto(
            product.id(),
            product.name(),
            product.price().amount(),
            product.description(),
            product.is_active(),
            product.createdAt(),
            product.updatedAt()
        );
    }

    public static Product fromCreate(
        java.util.UUID id,
        String name,
        java.math.BigDecimal price,
        String description,
        boolean isActive
    ) {
        return Product.createNewProduct(
            id,
            name,
            new Money(price),
            description,
            isActive
        );
    }

    public static Product applyUpdatePrice(Product product, java.math.BigDecimal price) {
        return product.withPrice(new Money(price));
    }
}
