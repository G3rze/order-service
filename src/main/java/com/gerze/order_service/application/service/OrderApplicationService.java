package com.gerze.order_service.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.gerze.order_service.application.command.AddItemCommand;
import com.gerze.order_service.application.command.ChangeStatusCommand;
import com.gerze.order_service.application.command.CreateOrderCommand;
import com.gerze.order_service.application.command.CreateOrderItemCommand;
import com.gerze.order_service.application.command.RemoveItemCommand;
import com.gerze.order_service.application.command.UpdateQuantityCommand;
import com.gerze.order_service.application.dto.OrderDto;
import com.gerze.order_service.application.dto.OrderSummaryDto;
import com.gerze.order_service.application.exception.NotFoundException;
import com.gerze.order_service.application.exception.ValidationException;
import com.gerze.order_service.application.mapper.OrderMapper;
import com.gerze.order_service.application.query.OrderQuery;
import com.gerze.order_service.application.query.PageRequest;
import com.gerze.order_service.application.usecase.AddItemUseCase;
import com.gerze.order_service.application.usecase.ChangeOrderStatusUseCase;
import com.gerze.order_service.application.usecase.CreateOrderUseCase;
import com.gerze.order_service.application.usecase.GetOrderUseCase;
import com.gerze.order_service.application.usecase.ListOrdersUseCase;
import com.gerze.order_service.application.usecase.RemoveItemUseCase;
import com.gerze.order_service.application.usecase.UpdateItemQuantityUseCase;
import com.gerze.order_service.domain.model.order.Order;
import com.gerze.order_service.domain.model.order.OrderItem;
import com.gerze.order_service.domain.model.shared.Quantity;
import com.gerze.order_service.domain.repository.OrderRepository;
import com.gerze.order_service.domain.repository.OrderStatusRepository;
import com.gerze.order_service.domain.repository.ProductRepository;
import com.gerze.order_service.domain.spec.SortedBy;
import com.gerze.order_service.domain.spec.filter.OrderFilter;

import jakarta.validation.Valid;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.validation.annotation.Validated;

@Validated
public class OrderApplicationService implements
    CreateOrderUseCase,
    AddItemUseCase,
    RemoveItemUseCase,
    UpdateItemQuantityUseCase,
    ChangeOrderStatusUseCase,
    GetOrderUseCase,
    ListOrdersUseCase {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderStatusRepository orderStatusRepository;

    public OrderApplicationService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        OrderStatusRepository orderStatusRepository
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.orderStatusRepository = Objects.requireNonNull(orderStatusRepository);
    }

    @Override
    public Mono<OrderDto> create(@Valid CreateOrderCommand command) {
        Objects.requireNonNull(command, "CreateOrderCommand cannot be null");

        UUID orderId = UUID.randomUUID();

        if (command.items().isEmpty()) {
            throw new ValidationException("Order must contain at least one item");
        }

        Set<UUID> productIds = command.items().stream()
            .map(CreateOrderItemCommand::productId)
            .collect(java.util.stream.Collectors.toSet());
        if (productIds.size() != command.items().size()) {
            throw new ValidationException("Duplicate products in order items are not allowed");
        }

        Mono<List<OrderItem>> itemsMono = Flux.fromIterable(command.items())
            .flatMap(itemCmd -> toOrderItem(orderId, itemCmd))
            .collectList();

        Mono<Order> orderMono = orderStatusRepository.findById(command.initialStatusId())
            .switchIfEmpty(Mono.error(new NotFoundException("Order status not found")))
            .zipWith(itemsMono, (status, items) -> Order.createNewOrder(orderId, command.customerId(), items, status));

        return orderMono
            .flatMap(orderRepository::save)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Mono<OrderDto> add(@Valid AddItemCommand command) {
        Objects.requireNonNull(command, "AddItemCommand cannot be null");

        Mono<Order> orderMono = findOrder(command.orderId());
        Mono<OrderItem> newItemMono = toOrderItem(command.orderId(), command.productId(), command.quantity());

        return Mono.zip(orderMono, newItemMono)
            .map(tuple -> {
                Order order = tuple.getT1();
                OrderItem newItem = tuple.getT2();
                boolean productAlreadyPresent = order.items().stream()
                    .anyMatch(item -> item.product().id().equals(newItem.product().id()));
                if (productAlreadyPresent) {
                    throw new ValidationException("Product already exists in the order; update quantity instead");
                }
                return order.addItem(newItem);
            })
            .flatMap(orderRepository::save)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Mono<OrderDto> remove(@Valid RemoveItemCommand command) {
        Objects.requireNonNull(command, "RemoveItemCommand cannot be null");

        return findOrder(command.orderId())
            .map(order -> order.removeItem(command.itemId()))
            .flatMap(orderRepository::save)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Mono<OrderDto> update(@Valid UpdateQuantityCommand command) {
        Objects.requireNonNull(command, "UpdateQuantityCommand cannot be null");

        return findOrder(command.orderId())
            .map(order -> order.updateItemQuantity(command.itemId(), new Quantity(command.quantity())))
            .flatMap(orderRepository::save)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Mono<OrderDto> changeStatus(@Valid ChangeStatusCommand command) {
        Objects.requireNonNull(command, "ChangeStatusCommand cannot be null");

        Mono<Order> orderMono = findOrder(command.orderId());
        Mono<com.gerze.order_service.domain.model.order.OrderStatus> statusMono = orderStatusRepository.findById(command.newStatusId())
            .switchIfEmpty(Mono.error(new NotFoundException("Order status not found")));

        return Mono.zip(orderMono, statusMono)
            .map(tuple -> tuple.getT1().withStatus(tuple.getT2()))
            .flatMap(orderRepository::save)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Mono<OrderDto> get(UUID orderId) {
        Objects.requireNonNull(orderId, "Order id cannot be null");

        return findOrder(orderId)
            .map(OrderMapper::toOrderDto);
    }

    @Override
    public Flux<OrderSummaryDto> list(OrderQuery query, @Valid PageRequest pageRequest) {
        OrderFilter filter = query == null
            ? null
            : new OrderFilter(
                query.customerId(),
                query.productId(),
                query.status(),
                query.createdAfter(),
                query.createdBefore(),
                query.minTotalAmount(),
                query.maxTotalAmount()
            );

        int page = pageRequest != null ? pageRequest.page() : 0;
        int size = pageRequest != null ? pageRequest.size() : 20;
        if (page < 0 || size <= 0) {
            throw new ValidationException("Page must be >= 0 and size must be > 0");
        }
        SortedBy sortedBy = pageRequest != null
            ? Objects.requireNonNull(pageRequest.sortedBy(), "sortedBy cannot be null")
            : new SortedBy("createdAt", org.springframework.data.domain.Sort.Direction.DESC);

        return orderRepository.findAll(filter, page, size, sortedBy)
            .map(OrderMapper::toOrderSummaryDto);
    }

    private Mono<Order> findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new NotFoundException("Order not found")));
    }

    private Mono<OrderItem> toOrderItem(UUID orderId, CreateOrderItemCommand itemCommand) {
        Objects.requireNonNull(itemCommand, "CreateOrderItemCommand cannot be null");
        return toOrderItem(orderId, itemCommand.productId(), itemCommand.quantity());
    }

    private Mono<OrderItem> toOrderItem(UUID orderId, UUID productId, int quantity) {
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new NotFoundException("Product not found")))
            .map(product -> {
                if (Boolean.FALSE.equals(product.is_active())) {
                    throw new ValidationException("Product is inactive");
                }
                return OrderItem.createNewOrderItem(
                UUID.randomUUID(),
                product,
                orderId,
                new Quantity(quantity)
            );
            });
    }

}
