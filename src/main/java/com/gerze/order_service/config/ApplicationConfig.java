package com.gerze.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gerze.order_service.application.service.OrderApplicationService;
import com.gerze.order_service.application.service.ProductApplicationService;
import com.gerze.order_service.domain.repository.OrderRepository;
import com.gerze.order_service.domain.repository.OrderStatusRepository;
import com.gerze.order_service.domain.repository.ProductRepository;

@Configuration
public class ApplicationConfig {

    @Bean
    public OrderApplicationService orderApplicationService(
        OrderRepository orderRepository,
        ProductRepository productRepository,
        OrderStatusRepository orderStatusRepository
    ) {
        return new OrderApplicationService(orderRepository, productRepository, orderStatusRepository);
    }

    @Bean
    public ProductApplicationService productApplicationService(ProductRepository productRepository) {
        return new ProductApplicationService(productRepository);
    }
}
