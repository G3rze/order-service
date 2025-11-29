package com.gerze.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.gerze.order_service.application.service.OrderApplicationService;
import com.gerze.order_service.application.service.ProductApplicationService;
import com.gerze.order_service.domain.repository.OrderRepository;
import com.gerze.order_service.domain.repository.OrderStatusRepository;
import com.gerze.order_service.domain.repository.ProductRepository;

import io.r2dbc.spi.ConnectionFactory;

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

    @Bean
    public ReactiveTransactionManager reactiveTransactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }
}
