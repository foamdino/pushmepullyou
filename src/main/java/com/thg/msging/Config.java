package com.thg.msging;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.springframework.amqp.core.*;
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



    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue(qName, true, false, false, args);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.thg.#");
    }

    @Bean
    public MessageChannel myInputChannel() {
        return new DirectChannel();
    }
    @Bean
    public PubSubInboundChannelAdapter messageChannelAdapter(
            @Qualifier("myInputChannel") MessageChannel inputChannel,
            PubSubTemplate pubSubTemplate) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, "msg-bg-data-relay");
        adapter.setOutputChannel(inputChannel);
        adapter.setPayloadType(String.class);
        adapter.setAckMode(AckMode.MANUAL);
        return adapter;
    }
}
