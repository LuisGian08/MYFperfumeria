package com.myf.sales.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "myf.events";
    public static final String SALE_QUEUE = "myf.sale.created";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue saleCreatedQueue() {
        return new Queue(SALE_QUEUE, true);
    }

    @Bean
    Binding saleCreatedBinding(Queue saleCreatedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(saleCreatedQueue).to(eventsExchange).with("sale.created");
    }
}