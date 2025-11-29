package com.gerze.order_service.infrastructure.r2dbc.adapter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;

import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.model.shared.Money;
import com.gerze.order_service.domain.repository.ProductRepository;
import com.gerze.order_service.domain.spec.SortedBy;
import com.gerze.order_service.infrastructure.r2dbc.entity.ProductEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final R2dbcEntityTemplate template;

    public ProductRepositoryAdapter(R2dbcEntityTemplate template) {
        this.template = Objects.requireNonNull(template);
    }

    @Override
    public Mono<Product> findById(UUID id) {
        return template.select(ProductEntity.class)
            .matching(org.springframework.data.relational.core.query.Query.query(
                org.springframework.data.relational.core.query.Criteria.where("id").is(id)
            ))
            .one()
            .map(this::toDomain);
    }

    @Override
    public Mono<Product> save(Product product) {
        ProductEntity entity = toEntity(product);
        String sql = """
            INSERT INTO product (id, name, price, description, is_active, created_at, updated_at)
            VALUES (:id, :name, :price, :description, :is_active, :created_at, :updated_at)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                price = EXCLUDED.price,
                description = EXCLUDED.description,
                is_active = EXCLUDED.is_active,
                updated_at = EXCLUDED.updated_at
            RETURNING *
            """;

        return bind(template.getDatabaseClient().sql(sql), entity)
            .map((row, meta) -> new ProductEntity(
                row.get("id", UUID.class),
                row.get("name", String.class),
                row.get("price", java.math.BigDecimal.class),
                row.get("description", String.class),
                row.get("is_active", Boolean.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class)
            ))
            .one()
            .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return template.delete(ProductEntity.class)
            .matching(org.springframework.data.relational.core.query.Query.query(
                org.springframework.data.relational.core.query.Criteria.where("id").is(id)
            ))
            .all()
            .then();
    }

    @Override
    public Flux<Product> findAll(int page, int size, SortedBy sortedBy) {
        String sortField = mapProductSortField(sortedBy.field());
        String direction = sortedBy.direction() == Direction.DESC ? "DESC" : "ASC";

        String sql = """
            SELECT id, name, price, description, is_active, created_at, updated_at
            FROM product
            ORDER BY %s %s
            LIMIT :limit OFFSET :offset
            """.formatted(sortField, direction);

        return template.getDatabaseClient().sql(sql)
            .bind("limit", size)
            .bind("offset", page * size)
            .map((row, meta) -> new ProductEntity(
                row.get("id", UUID.class),
                row.get("name", String.class),
                row.get("price", java.math.BigDecimal.class),
                row.get("description", String.class),
                row.get("is_active", Boolean.class),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class)
            ))
            .all()
            .map(this::toDomain);
    }

    private GenericExecuteSpec bind(GenericExecuteSpec spec, ProductEntity entity) {
        return spec.bind("id", entity.id())
            .bind("name", entity.name())
            .bind("price", entity.price())
            .bind("description", entity.description())
            .bind("is_active", entity.isActive())
            .bind("created_at", entity.createdAt())
            .bind("updated_at", entity.updatedAt());
    }

    private Product toDomain(ProductEntity entity) {
        return new Product(
            entity.id(),
            entity.name(),
            new Money(entity.price()),
            entity.description(),
            entity.isActive(),
            entity.createdAt(),
            entity.updatedAt()
        );
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
            product.id(),
            product.name(),
            product.price().amount(),
            product.description(),
            product.is_active(),
            product.createdAt(),
            product.updatedAt()
        );
    }

    private String mapProductSortField(String field) {
        return switch (field) {
            case "name" -> "name";
            case "price" -> "price";
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            default -> "created_at";
        };
    }
}
