package com.gerze.order_service.application.usecase;

import com.gerze.order_service.application.dto.OrderSummaryDto;
import com.gerze.order_service.application.query.OrderQuery;
import com.gerze.order_service.application.query.PageRequest;

import reactor.core.publisher.Flux;

public interface ListOrdersUseCase {
    Flux<OrderSummaryDto> list(OrderQuery query, PageRequest pageRequest);
}
