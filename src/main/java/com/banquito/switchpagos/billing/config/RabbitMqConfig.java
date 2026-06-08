package com.banquito.switchpagos.billing.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange billingExchange(@Value("${rabbit.exchange.billing}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue billingBatchCompletedQueue(@Value("${rabbit.queue.billing.batch-completed}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue reportingBillingCompletedQueue(@Value("${rabbit.queue.reporting.billing-completed}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding batchLinesCompletedBinding(
            TopicExchange billingExchange,
            Queue billingBatchCompletedQueue,
            @Value("${rabbit.routing-key.batch-lines-completed}") String routingKey) {
        return BindingBuilder.bind(billingBatchCompletedQueue).to(billingExchange).with(routingKey);
    }

    @Bean
    public Binding billingCompletedBinding(
            TopicExchange billingExchange,
            Queue reportingBillingCompletedQueue,
            @Value("${rabbit.routing-key.billing-completed}") String routingKey) {
        return BindingBuilder.bind(reportingBillingCompletedQueue).to(billingExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        JacksonJsonMessageConverter messageConverter = new JacksonJsonMessageConverter();
        messageConverter.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        return messageConverter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, JacksonJsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
