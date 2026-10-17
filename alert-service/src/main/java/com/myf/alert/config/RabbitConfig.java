package com.myf.alert.config;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.*;
@Configuration
public class RabbitConfig {
    public static final String EXCHANGE="myf.events", LOW_QUEUE="myf.stock.low", RECOVERY_QUEUE="myf.stock.recovered";
    @Bean TopicExchange ex(){return new TopicExchange(EXCHANGE,true,false);}
    @Bean Queue lowQueue(){return new Queue(LOW_QUEUE,true);}
    @Bean Queue recoveryQueue(){return new Queue(RECOVERY_QUEUE,true);}
    @Bean Binding lowBinding(Queue lowQueue, TopicExchange ex){return BindingBuilder.bind(lowQueue).to(ex).with("stock.low");}
    @Bean Binding recoveryBinding(Queue recoveryQueue, TopicExchange ex){return BindingBuilder.bind(recoveryQueue).to(ex).with("stock.recovered");}
}
