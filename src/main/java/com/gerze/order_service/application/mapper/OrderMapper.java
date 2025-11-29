package com.gerze.order_service.application.mapper;

import java.util.List;

import com.gerze.order_service.application.dto.OrderDto;
import com.gerze.order_service.application.dto.OrderItemDto;
import com.gerze.order_service.application.dto.OrderStatusDto;
import com.gerze.order_service.application.dto.OrderSummaryDto;
import com.gerze.order_service.domain.model.order.Order;
import com.gerze.order_service.domain.model.order.OrderItem;
import com.gerze.order_service.domain.model.order.OrderStatus;

public class OrderMapper {

    private OrderMapper() {
    }

    public static OrderDto toOrderDto(Order order) {
        List<OrderItemDto> itemDtos = order.items().stream()
            .map(OrderMapper::toOrderItemDto)
            .toList();

        return new OrderDto(
            order.id(),
            order.customerId(),
            toStatusDto(order.status()),
            itemDtos,
            order.totalAmount().amount(),
            order.createdAt(),
            order.updatedAt()
        );
    }

    public static OrderSummaryDto toOrderSummaryDto(Order order) {
        return new OrderSummaryDto(
            order.id(),
            order.customerId(),
            toStatusDto(order.status()),
            order.totalAmount().amount(),
            order.createdAt()
        );
    }

    private static OrderItemDto toOrderItemDto(OrderItem item) {
        return new OrderItemDto(
            item.id(),
            item.product().id(),
            item.quantity().value(),
            item.lineTotal().amount()
        );
    }

    private static OrderStatusDto toStatusDto(OrderStatus status) {
        return new OrderStatusDto(
            status.id(),
            status.code(),
            status.label(),
            status.description(),
            status.isFinal()
        );
    }
}
