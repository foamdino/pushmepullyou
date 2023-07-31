package com.thg.msging;


import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

@Service
public class PubsubConsumer {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final Logger logger = LoggerFactory.getLogger(PubsubConsumer.class);

    private final Config config;

    private Counter consumeMsg;
    private Counter consumeMsgExceptions;

    private final RabbitMQProducer producer;

    @Autowired
    public PubsubConsumer(Config config, RabbitMQProducer producer, MeterRegistry meterRegistry) {
        this.config = config;
        this.producer = producer;
        consumeMsg = meterRegistry.counter("consumeMsg.success");
        consumeMsgExceptions = meterRegistry.counter("consumeMsg.exceptions");
    }

    @ServiceActivator(inputChannel = "mySubscriptionInputChannel")
    public void consume(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        logger.info(String.format("Message received: %s", payload));
        producer.send(payload);
        logger.info(String.format("Message forwarded: %s", payload));
    }
}
