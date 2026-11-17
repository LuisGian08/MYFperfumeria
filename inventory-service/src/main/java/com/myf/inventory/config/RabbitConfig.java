package com.myf.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "myf.events";
    public static final String LOW_QUEUE = "myf.stock.low";
    public static final String RECOVERY_QUEUE = "myf.stock.recovered";

    @Bean
    TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue lowQueue() {
        return new Queue(LOW_QUEUE, true);
    }

    @Bean
    Queue recoveryQueue() {
        return new Queue(RECOVERY_QUEUE, true);
    }

    @Bean
    Binding lowBinding(Queue lowQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(lowQueue).to(eventsExchange).with("stock.low");
    }

    @Bean
    Binding recoveryBinding(Queue recoveryQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(recoveryQueue).to(eventsExchange).with("stock.recovered");
    }
}
