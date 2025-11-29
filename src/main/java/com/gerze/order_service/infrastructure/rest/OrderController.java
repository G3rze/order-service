package com.gerze.order_service.infrastructure.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import com.gerze.order_service.application.command.AddItemCommand;
import com.gerze.order_service.application.command.ChangeStatusCommand;
import com.gerze.order_service.application.command.CreateOrderCommand;
import com.gerze.order_service.application.command.CreateOrderItemCommand;
import com.gerze.order_service.application.command.RemoveItemCommand;
import com.gerze.order_service.application.command.UpdateQuantityCommand;
import com.gerze.order_service.application.dto.OrderDto;
import com.gerze.order_service.application.dto.OrderSummaryDto;
import com.gerze.order_service.application.query.OrderQuery;
import com.gerze.order_service.application.query.PageRequest;
import com.gerze.order_service.application.usecase.AddItemUseCase;
import com.gerze.order_service.application.usecase.ChangeOrderStatusUseCase;
import com.gerze.order_service.application.usecase.CreateOrderUseCase;
import com.gerze.order_service.application.usecase.GetOrderUseCase;
import com.gerze.order_service.application.usecase.ListOrdersUseCase;
import com.gerze.order_service.application.usecase.RemoveItemUseCase;
import com.gerze.order_service.application.usecase.UpdateItemQuantityUseCase;
import com.gerze.order_service.domain.spec.SortedBy;
import com.gerze.order_service.infrastructure.rest.dto.OrderRequests.AddItemRequest;
import com.gerze.order_service.infrastructure.rest.dto.OrderRequests.ChangeStatusRequest;
import com.gerze.order_service.infrastructure.rest.dto.OrderRequests.CreateOrderItemRequest;
import com.gerze.order_service.infrastructure.rest.dto.OrderRequests.CreateOrderRequest;
import com.gerze.order_service.infrastructure.rest.dto.OrderRequests.UpdateQuantityRequest;

import jakarta.validation.Valid;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final CreateOrderUseCase createOrder;
    private final AddItemUseCase addItem;
    private final RemoveItemUseCase removeItem;
    private final UpdateItemQuantityUseCase updateItemQuantity;
    private final ChangeOrderStatusUseCase changeOrderStatus;
    private final GetOrderUseCase getOrder;
    private final ListOrdersUseCase listOrders;

    public OrderController(
        CreateOrderUseCase createOrder,
        AddItemUseCase addItem,
        RemoveItemUseCase removeItem,
        UpdateItemQuantityUseCase updateItemQuantity,
        ChangeOrderStatusUseCase changeOrderStatus,
        GetOrderUseCase getOrder,
        ListOrdersUseCase listOrders
    ) {
        this.createOrder = createOrder;
        this.addItem = addItem;
        this.removeItem = removeItem;
        this.updateItemQuantity = updateItemQuantity;
        this.changeOrderStatus = changeOrderStatus;
        this.getOrder = getOrder;
        this.listOrders = listOrders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = new CreateOrderCommand(
            request.customerId(),
            request.items().stream()
                .map(this::toCreateItemCommand)
                .toList(),
            request.initialStatusId()
        );
        return createOrder.create(command);
    }

    @PostMapping("/{orderId}/items")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderDto> addItem(
        @PathVariable UUID orderId,
        @Valid @RequestBody AddItemRequest request
    ) {
        AddItemCommand command = new AddItemCommand(orderId, request.productId(), request.quantity());
        return addItem.add(command);
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeItem(
        @PathVariable UUID orderId,
        @PathVariable UUID itemId
    ) {
        RemoveItemCommand command = new RemoveItemCommand(orderId, itemId);
        return removeItem.remove(command).then();
    }

    @PatchMapping("/{orderId}/items/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderDto> updateItemQuantity(
        @PathVariable UUID orderId,
        @PathVariable UUID itemId,
        @Valid @RequestBody UpdateQuantityRequest request
    ) {
        UpdateQuantityCommand command = new UpdateQuantityCommand(orderId, itemId, request.quantity());
        return updateItemQuantity.update(command);
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderDto> changeStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody ChangeStatusRequest request
    ) {
        ChangeStatusCommand command = new ChangeStatusCommand(orderId, request.newStatusId());
        return changeOrderStatus.changeStatus(command);
    }

    @GetMapping("/{orderId}")
    public Mono<OrderDto> get(@PathVariable UUID orderId) {
        return getOrder.get(orderId);
    }

    @GetMapping
    public Flux<OrderSummaryDto> list(
        @RequestParam(value = "customerId", required = false) UUID customerId,
        @RequestParam(value = "productId", required = false) UUID productId,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "createdAfter", required = false) Instant createdAfter,
        @RequestParam(value = "createdBefore", required = false) Instant createdBefore,
        @RequestParam(value = "minTotalAmount", required = false) BigDecimal minTotalAmount,
        @RequestParam(value = "maxTotalAmount", required = false) BigDecimal maxTotalAmount,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
        @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir
    ) {
        OrderQuery query = new OrderQuery(
            customerId,
            productId,
            status,
            createdAfter,
            createdBefore,
            minTotalAmount,
            maxTotalAmount
        );

        SortedBy sortedBy = new SortedBy(sortField, parseDirection(sortDir));
        PageRequest pageRequest = new PageRequest(page, size, sortedBy);

        return listOrders.list(query, pageRequest);
    }

    private CreateOrderItemCommand toCreateItemCommand(CreateOrderItemRequest request) {
        return new CreateOrderItemCommand(request.productId(), request.quantity());
    }

    private org.springframework.data.domain.Sort.Direction parseDirection(String dir) {
        return "ASC".equalsIgnoreCase(dir) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC;
    }
}
