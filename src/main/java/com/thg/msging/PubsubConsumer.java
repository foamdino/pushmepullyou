package com.thg.msging;


import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import com.thg.msging.producers.*;
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
    private final HutalyticsProducer hutalyticsProducer;
    private final Elysium2FrontEndProducer elysium2FrontEndProducer;
    private final Elysium2BackEndProducer elysium2BackEndProducer;
    private final OrderEventsProducer orderEventsProducer;
    private final FrontEndCheckoutProducer frontEndCheckoutProducer;
    private final FrontEndElysiumPerfProducer frontEndElysiumPerfProducer;

    @Autowired
    public PubsubConsumer(Config config,
                          MeterRegistry meterRegistry,
                          HutalyticsProducer hutalyticsProducer,
                          Elysium2FrontEndProducer elysium2FrontEndProducer,
                          Elysium2BackEndProducer elysium2BackEndProducer,
                          OrderEventsProducer orderEventsProducer,
                          FrontEndCheckoutProducer frontEndCheckoutProducer,
                          FrontEndElysiumPerfProducer frontEndElysiumPerfProducer) {
        this.config = config;
        this.hutalyticsProducer = hutalyticsProducer;
        this.elysium2FrontEndProducer = elysium2FrontEndProducer;
        this.elysium2BackEndProducer = elysium2BackEndProducer;
        this.orderEventsProducer = orderEventsProducer;
        this.frontEndCheckoutProducer = frontEndCheckoutProducer;
        this.frontEndElysiumPerfProducer = frontEndElysiumPerfProducer;
        consumeMsg = meterRegistry.counter("consumeMsg.success");
        consumeMsgExceptions = meterRegistry.counter("consumeMsg.exceptions");
    }

    @ServiceActivator(inputChannel = "hutalytics-expanded")
    public void consumeHutalytics(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        hutalyticsProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for hutalytics-expanded: %s", payload));
    }

    @ServiceActivator(inputChannel = "elysium2-frontend-enriched")
    public void consumeEly2FrontEnd(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        elysium2FrontEndProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for elysium2-frontend-enriched: %s", payload));
    }

    @ServiceActivator(inputChannel = "elysium2-backend-events")
    public void consumeEly2BackEndEvents(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        elysium2BackEndProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for elysium2-backend-events: %s", payload));
    }

    @ServiceActivator(inputChannel = "order-events-expanded")
    public void consumeOrderEvents(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        orderEventsProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for order-events-expanded: %s", payload));
    }

    @ServiceActivator(inputChannel = "frontend-checkout-events")
    public void consumeFrontEndCheckoutEvents(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        frontEndCheckoutProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for frontend-checkout-events: %s", payload));
    }

    @ServiceActivator(inputChannel = "frontend-elysium-perf-data")
    public void consumeFrontEndElyPerfData(String payload, @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
        frontEndElysiumPerfProducer.send(payload);
        message.ack();
        logger.trace(String.format("Message forwarded for frontend-elysium-perf-data: %s", payload));
    }
}
