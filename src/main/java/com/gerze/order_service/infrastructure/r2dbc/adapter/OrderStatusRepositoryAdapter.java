package com.gerze.order_service.infrastructure.r2dbc.adapter;

import java.util.Objects;
import java.util.UUID;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;

import com.gerze.order_service.domain.model.order.OrderStatus;
import com.gerze.order_service.domain.repository.OrderStatusRepository;
import com.gerze.order_service.infrastructure.r2dbc.entity.OrderStatusEntity;

import reactor.core.publisher.Mono;

@Repository
public class OrderStatusRepositoryAdapter implements OrderStatusRepository {

    private final R2dbcEntityTemplate template;

    public OrderStatusRepositoryAdapter(R2dbcEntityTemplate template) {
        this.template = Objects.requireNonNull(template);
    }

    @Override
    public Mono<OrderStatus> findById(UUID id) {
        return template.select(OrderStatusEntity.class)
            .matching(org.springframework.data.relational.core.query.Query.query(
                org.springframework.data.relational.core.query.Criteria.where("id").is(id)
            ))
            .one()
            .map(this::toDomain);
    }

    private OrderStatus toDomain(OrderStatusEntity entity) {
        return new OrderStatus(
            entity.id(),
            entity.code(),
            entity.label(),
            entity.description(),
            entity.isFinal(),
            entity.createdAt()
        );
    }
}
