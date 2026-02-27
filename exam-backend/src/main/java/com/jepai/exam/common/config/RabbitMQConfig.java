package com.jepai.exam.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置 - 用于异步评卷任务
 */
@Configuration
public class RabbitMQConfig {

    /** 评卷任务队列 */
    public static final String GRADING_QUEUE = "exam.grading.queue";
    /** 评卷任务死信队列 */
    public static final String GRADING_DLQ = "exam.grading.dlq";
    /** 评卷任务交换机 */
    public static final String GRADING_EXCHANGE = "exam.grading.exchange";
    /** 评卷任务路由键 */
    public static final String GRADING_ROUTING_KEY = "grading";

    @Bean
    public Queue gradingQueue() {
        return QueueBuilder.durable(GRADING_QUEUE)
                .withArgument("x-dead-letter-exchange", GRADING_DLQ)
                .build();
    }

    @Bean
    public Queue gradingDeadLetterQueue() {
        return QueueBuilder.durable(GRADING_DLQ).build();
    }

    @Bean
    public DirectExchange gradingExchange() {
        return new DirectExchange(GRADING_EXCHANGE);
    }

    @Bean
    public Binding gradingBinding() {
        return BindingBuilder.bind(gradingQueue()).to(gradingExchange()).with(GRADING_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
