package com.thg.msging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Config {

    public String qName;
    public String exchange;
    public int messages;
    private String host;
    private String username;
    private String password;

    public Config(@Value("${q}") String qName,
                  @Value("${exchange}") String exchange,
                  @Value("${messages}") int messages,
                  @Value("${spring.rabbitmq.host}") String host,
                  @Value("${spring.rabbitmq.username}") String username,
                  @Value("${spring.rabbitmq.password}") String password) {
        this.qName = qName;
        this.exchange = exchange;
        this.messages = messages;
        this.host = host;
        this.username = username;
        this.password = password;
    }

//    @Bean
//    public MeterRegistry getMeterRegistry() {
//        CompositeMeterRegistry meterRegistry = new CompositeMeterRegistry();
//        return meterRegistry;
//    }

    /*
    Subscriptions:
    topic hutalytics-expanded -  for ely 1:   msg-bg-data-hutalytics-relay
    topics elysium2-frontend-enriched & elysium2-backend-events for ely2: msg-bg-data-ely2-fe-relay & msg-bg-data-ely2-be-relay
    topic order-events-expanded: msg-bg-data-order-events-relay
    topic frontend-checkout-events: msg-bg-data-fe-checkout-events-relay
    topic frontend-elysium-perf-data: msg-bg-data-fe-perf-relay
     */

    @Bean("hutalytics-expanded-q")
    public Queue hutalyticsExpandedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("hutalytics-expanded", true, false, false, args);
    }

    @Bean("elysium2-frontend-enriched-q")
    public Queue elysiym2FrontEndEnrichedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("elysium2-frontend-enriched", true, false, false, args);
    }

    @Bean("elysium2-backend-events-q")
    public Queue elysium2BackEndEventsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("elysium2-backend-events", true, false, false, args);
    }

    @Bean("order-events-expanded-q")
    public Queue orderEventsExpandedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("order-events-expanded", true, false, false, args);
    }

    @Bean("frontend-checkout-events-q")
    public Queue frontEndCheckoutEventsQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("frontend-checkout-events", true, false, false, args);
    }

    @Bean("frontend-elysium-perf-data-q")
    public Queue frontEndElysiumPerfDataqueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue("frontend-elysium-perf-data", true, false, false, args);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding hutalyticsBinding(@Qualifier("hutalytics-expanded-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.hutalytics-expanded-q");
    }
    @Bean
    public Binding ely2FrontendBinding(@Qualifier("elysium2-frontend-enriched-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.elysium2-frontend-enriched-q");
    }
    @Bean
    public Binding ely2BackendBinding(@Qualifier("elysium2-backend-events-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.elysium2-backend-events-q");
    }
    @Bean
    public Binding orderEventsBinding(@Qualifier("order-events-expanded-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.order-events-expanded-q");
    }
    @Bean
    public Binding checkoutEventsBinding(@Qualifier("frontend-checkout-events-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.frontend-checkout-events-q");
    }
    @Bean
    public Binding elysiumPerfBinding(@Qualifier("frontend-elysium-perf-data-q") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.frontend-elysium-perf-data-q");
    }

    @Bean
    public MessageChannel myInputChannel() {
        return new DirectChannel();
    }

    @Bean("hutalytics-expanded")
    public MessageChannel hutalytics() {
        return new DirectChannel();
    }

    @Bean("elysium2-frontend-enriched")
    public MessageChannel elysium2FrontEndEnriched() {
        return new DirectChannel();
    }

    @Bean("elysium2-backend-events")
    public MessageChannel elysium2BackEndEvents() {
        return new DirectChannel();
    }

    @Bean("order-events-expanded")
    public MessageChannel orderEventsExpanded() {
        return new DirectChannel();
    }

    @Bean("frontend-checkout-events")
    public MessageChannel frontEndCheckoutEvents() {
        return new DirectChannel();
    }

    @Bean("frontend-elysium-perf-data")
    public MessageChannel frontEndElysiumPerfData() {
        return new DirectChannel();
    }

    @Bean("hutalytics-expanded-template")
    public RabbitTemplate hutalyticsRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean("elysium2-frontend-enriched-template")
    public RabbitTemplate ely2FrontEndRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean("elysium2-backend-events-template")
    public RabbitTemplate ely2BackEndRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean("order-events-expanded-template")
    public RabbitTemplate orderEventsRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean("frontend-checkout-events-template")
    public RabbitTemplate frontEndCheckoutRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean("frontend-elysium-perf-data-template")
    public RabbitTemplate frontEndElyPerfDataRabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public PubSubInboundChannelAdapter hutalyticsRelay(
            @Qualifier("hutalytics-expanded") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-hutalytics-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    public PubSubInboundChannelAdapter ely2FrontEndRelay(
            @Qualifier("elysium2-frontend-enriched") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-ely2-fe-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    public PubSubInboundChannelAdapter ely2BackEndRelay(
            @Qualifier("elysium2-backend-events") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-ely2-be-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    public PubSubInboundChannelAdapter orderEventsRelay(
            @Qualifier("order-events-expanded") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-order-events-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    public PubSubInboundChannelAdapter checkoutEventsRelay(
            @Qualifier("frontend-checkout-events") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-fe-checkout-events-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }

    @Bean
    public PubSubInboundChannelAdapter frontEndPerfRelay(
            @Qualifier("frontend-elysium-perf-data") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-fe-perf-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }
}
