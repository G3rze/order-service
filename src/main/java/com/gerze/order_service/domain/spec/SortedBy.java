package com.gerze.order_service.domain.spec;

import org.springframework.data.domain.Sort.Direction;

public record SortedBy(
    String field,
    Direction direction
) {}
