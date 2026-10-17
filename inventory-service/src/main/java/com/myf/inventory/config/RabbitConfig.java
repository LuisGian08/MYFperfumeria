package com.myf.inventory.config; import org.springframework.amqp.core.*; import org.springframework.context.annotation.*;
@Configuration public class RabbitConfig { public static final String EXCHANGE="myf.events"; @Bean TopicExchange eventsExchange(){return new TopicExchange(EXCHANGE,true,false);} }
