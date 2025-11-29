package com.gerze.order_service.infrastructure.r2dbc.adapter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.gerze.order_service.domain.model.order.Order;
import com.gerze.order_service.domain.model.order.OrderItem;
import com.gerze.order_service.domain.model.order.OrderStatus;
import com.gerze.order_service.domain.model.product.Product;
import com.gerze.order_service.domain.model.shared.Money;
import com.gerze.order_service.domain.model.shared.Quantity;
import com.gerze.order_service.domain.repository.OrderRepository;
import com.gerze.order_service.domain.spec.SortedBy;
import com.gerze.order_service.domain.spec.filter.OrderFilter;
import com.gerze.order_service.infrastructure.r2dbc.entity.OrderEntity;
import com.gerze.order_service.infrastructure.r2dbc.entity.OrderItemEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final R2dbcEntityTemplate template;
    private final TransactionalOperator tx;

    public OrderRepositoryAdapter(R2dbcEntityTemplate template, TransactionalOperator tx) {
        this.template = Objects.requireNonNull(template);
        this.tx = Objects.requireNonNull(tx);
    }

    @Override
    public Mono<Order> save(Order order) {
        Objects.requireNonNull(order, "order cannot be null");
        OrderEntity entity = toEntity(order);

        String upsertOrder = """
            INSERT INTO orders (id, customer_id, status_id, total_amount, created_at, updated_at)
            VALUES (:id, :customer_id, :status_id, :total_amount, :created_at, :updated_at)
            ON CONFLICT (id) DO UPDATE
            SET customer_id = EXCLUDED.customer_id,
                status_id = EXCLUDED.status_id,
                total_amount = EXCLUDED.total_amount,
                updated_at = EXCLUDED.updated_at
            """;

        Mono<Void> upsertOrderMono = bindOrder(template.getDatabaseClient().sql(upsertOrder), entity)
            .then();

        Mono<Void> deleteItems = template.getDatabaseClient()
            .sql("DELETE FROM order_items WHERE order_id = :order_id")
            .bind("order_id", order.id())
            .then();

        Flux<Void> insertItems = Flux.fromIterable(order.items())
            .flatMap(item -> {
                OrderItemEntity itemEntity = toEntity(order.id(), item);
                String insertSql = """
                    INSERT INTO order_items (id, order_id, product_id, product_name_snapshot, product_unit_price_snapshot, quantity, line_total)
                    VALUES (:id, :order_id, :product_id, :product_name_snapshot, :product_unit_price_snapshot, :quantity, :line_total)
                    """;
                return bindItem(template.getDatabaseClient().sql(insertSql), itemEntity).then();
            });

        return Mono.when(upsertOrderMono, deleteItems.thenMany(insertItems).then())
            .then(findById(order.id()))
            .as(tx::transactional);
    }

    @Override
    public Mono<Order> findById(UUID id) {
        return fetchOrderHeader(id)
            .zipWith(fetchItems(id).collectList())
            .map(tuple -> new Order(
                tuple.getT1().id(),
                tuple.getT1().customerId(),
                tuple.getT1().status(),
                List.copyOf(tuple.getT2()),
                tuple.getT1().totalAmount(),
                tuple.getT1().createdAt(),
                tuple.getT1().updatedAt()
            ));
    }

    @Override
    public Flux<Order> findAll(OrderFilter filter, int page, int size, SortedBy sortedBy) {
        StringBuilder sql = new StringBuilder("""
            SELECT o.id, o.customer_id, o.status_id, o.total_amount, o.created_at, o.updated_at,
                   s.code AS status_code, s.label AS status_label, s.description AS status_description, s.is_final AS status_is_final, s.created_at AS status_created_at
            FROM orders o
            JOIN order_status s ON s.id = o.status_id
            """);

        String where = buildWhere(filter);
        if (!where.isBlank()) {
            sql.append(" WHERE ").append(where);
        }

        String sortField = mapOrderSortField(sortedBy.field());
        String direction = sortedBy.direction() == Direction.DESC ? "DESC" : "ASC";
        sql.append(" ORDER BY ").append(sortField).append(" ").append(direction);
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient().sql(sql.toString())
            .bind("limit", size)
            .bind("offset", page * size);
        spec = bindFilter(spec, filter);

        return spec.map((row, meta) -> new OrderHeader(
            row.get("id", UUID.class),
            row.get("customer_id", UUID.class),
            new OrderStatus(
                row.get("status_id", UUID.class),
                row.get("status_code", String.class),
                row.get("status_label", String.class),
                row.get("status_description", String.class),
                row.get("status_is_final", Boolean.class),
                row.get("status_created_at", Instant.class)
            ),
            new Money(row.get("total_amount", java.math.BigDecimal.class)),
            row.get("created_at", Instant.class),
            row.get("updated_at", Instant.class)
        )).all()
        .flatMap(header -> fetchItems(header.id())
            .collectList()
            .map(items -> new Order(
                header.id(),
                header.customerId(),
                header.status(),
                List.copyOf(items),
                header.totalAmount(),
                header.createdAt(),
                header.updatedAt()
            )));
    }

    private Mono<OrderHeader> fetchOrderHeader(UUID id) {
        String sql = """
            SELECT o.id, o.customer_id, o.status_id, o.total_amount, o.created_at, o.updated_at,
                   s.code AS status_code, s.label AS status_label, s.description AS status_description, s.is_final AS status_is_final, s.created_at AS status_created_at
            FROM orders o
            JOIN order_status s ON s.id = o.status_id
            WHERE o.id = :id
            """;
        return template.getDatabaseClient().sql(sql)
            .bind("id", id)
            .map((row, meta) -> new OrderHeader(
                row.get("id", UUID.class),
                row.get("customer_id", UUID.class),
                new OrderStatus(
                    row.get("status_id", UUID.class),
                    row.get("status_code", String.class),
                    row.get("status_label", String.class),
                    row.get("status_description", String.class),
                    row.get("status_is_final", Boolean.class),
                    row.get("status_created_at", Instant.class)
                ),
                new Money(row.get("total_amount", java.math.BigDecimal.class)),
                row.get("created_at", Instant.class),
                row.get("updated_at", Instant.class)
            ))
            .one();
    }

    private Flux<OrderItem> fetchItems(UUID orderId) {
        String sql = """
            SELECT oi.id,
                   oi.order_id,
                   oi.product_id,
                   oi.product_name_snapshot,
                   oi.product_unit_price_snapshot,
                   oi.quantity,
                   oi.line_total,
                   p.name AS product_name,
                   p.price AS product_price,
                   p.description AS product_description,
                   p.is_active AS product_is_active,
                   p.created_at AS product_created_at,
                   p.updated_at AS product_updated_at
            FROM order_items oi
            LEFT JOIN product p ON p.id = oi.product_id
            WHERE oi.order_id = :order_id
            """;
        return template.getDatabaseClient().sql(sql)
            .bind("order_id", orderId)
            .map((row, meta) -> {
                String name = row.get("product_name_snapshot", String.class);
                java.math.BigDecimal priceSnapshot = row.get("product_unit_price_snapshot", java.math.BigDecimal.class);
                String description = row.get("product_description", String.class);
                Boolean isActive = row.get("product_is_active", Boolean.class);
                Instant productCreatedAt = row.get("product_created_at", Instant.class);
                Instant productUpdatedAt = row.get("product_updated_at", Instant.class);

                Product product = new Product(
                    row.get("product_id", UUID.class),
                    name,
                    new Money(priceSnapshot),
                    description,
                    isActive != null ? isActive : Boolean.TRUE,
                    productCreatedAt != null ? productCreatedAt : Instant.EPOCH,
                    productUpdatedAt != null ? productUpdatedAt : Instant.EPOCH
                );
                return new OrderItem(
                    row.get("id", UUID.class),
                    product,
                    row.get("order_id", UUID.class),
                    new Quantity(row.get("quantity", Integer.class)),
                    new Money(row.get("line_total", java.math.BigDecimal.class))
                );
            })
            .all();
    }

    private GenericExecuteSpec bindOrder(GenericExecuteSpec spec, OrderEntity entity) {
        return spec.bind("id", entity.id())
            .bind("customer_id", entity.customerId())
            .bind("status_id", entity.statusId())
            .bind("total_amount", entity.totalAmount())
            .bind("created_at", entity.createdAt())
            .bind("updated_at", entity.updatedAt());
    }

    private GenericExecuteSpec bindItem(GenericExecuteSpec spec, OrderItemEntity entity) {
        return spec.bind("id", entity.id())
            .bind("order_id", entity.orderId())
            .bind("product_id", entity.productId())
            .bind("product_name_snapshot", entity.productNameSnapshot())
            .bind("product_unit_price_snapshot", entity.productUnitPriceSnapshot())
            .bind("quantity", entity.quantity())
            .bind("line_total", entity.lineTotal());
    }

    private OrderEntity toEntity(Order order) {
        return new OrderEntity(
            order.id(),
            order.customerId(),
            order.status().id(),
            order.totalAmount().amount(),
            order.createdAt(),
            order.updatedAt()
        );
    }

    private OrderItemEntity toEntity(UUID orderId, OrderItem item) {
        return new OrderItemEntity(
            item.id(),
            orderId,
            item.product().id(),
            item.product().name(),
            item.product().price().amount(),
            item.quantity().value(),
            item.lineTotal().amount()
        );
    }

    private String mapOrderSortField(String field) {
        return switch (field) {
            case "createdAt" -> "o.created_at";
            case "updatedAt" -> "o.updated_at";
            case "totalAmount" -> "o.total_amount";
            default -> "o.created_at";
        };
    }

    private String buildWhere(OrderFilter filter) {
        if (filter == null) {
            return "";
        }
        var clauses = new java.util.ArrayList<String>();
        if (filter.customerId() != null) {
            clauses.add("o.customer_id = :customer_id");
        }
        if (filter.productId() != null) {
            clauses.add("EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id AND oi.product_id = :product_id)");
        }
        if (filter.status() != null) {
            clauses.add("s.code = :status_code");
        }
        if (filter.createdAfter() != null) {
            clauses.add("o.created_at >= :created_after");
        }
        if (filter.createdBefore() != null) {
            clauses.add("o.created_at <= :created_before");
        }
        if (filter.minTotalAmount() != null) {
            clauses.add("o.total_amount >= :min_total");
        }
        if (filter.maxTotalAmount() != null) {
            clauses.add("o.total_amount <= :max_total");
        }
        return clauses.stream().collect(Collectors.joining(" AND "));
    }

    private GenericExecuteSpec bindFilter(GenericExecuteSpec spec, OrderFilter filter) {
        if (filter == null) {
            return spec;
        }
        if (filter.customerId() != null) {
            spec = spec.bind("customer_id", filter.customerId());
        }
        if (filter.productId() != null) {
            spec = spec.bind("product_id", filter.productId());
        }
        if (filter.status() != null) {
            spec = spec.bind("status_code", filter.status());
        }
        if (filter.createdAfter() != null) {
            spec = spec.bind("created_after", filter.createdAfter());
        }
        if (filter.createdBefore() != null) {
            spec = spec.bind("created_before", filter.createdBefore());
        }
        if (filter.minTotalAmount() != null) {
            spec = spec.bind("min_total", filter.minTotalAmount());
        }
        if (filter.maxTotalAmount() != null) {
            spec = spec.bind("max_total", filter.maxTotalAmount());
        }
        return spec;
    }

    private record OrderHeader(
        UUID id,
        UUID customerId,
        OrderStatus status,
        Money totalAmount,
        Instant createdAt,
        Instant updatedAt
    ) {}
}
