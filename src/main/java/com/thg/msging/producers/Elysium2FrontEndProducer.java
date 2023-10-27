package com.thg.msging.producers;

import com.thg.msging.Config;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Elysium2FrontEndProducer {

    private final Logger logger = LoggerFactory.getLogger(Elysium2FrontEndProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final Config config;
    private Counter produceMsgAttempts;
    private Counter producMsgAcks;
    private Counter produceMsgNacks;
    private Counter produceMsgExceptions;

    @Autowired
    public Elysium2FrontEndProducer(@Qualifier("elysium2-frontend-enriched-template")RabbitTemplate rabbitTemplate, Config config, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
        produceMsgAttempts = meterRegistry.counter("produceMsg.attempts", "exchange", config.exchange, "queue", "elysium2-frontend-enriched");
        producMsgAcks = meterRegistry.counter("produceMsg.acks", "exchange", config.exchange, "queue", "elysium2-frontend-enriched");
        produceMsgNacks = meterRegistry.counter("produceMsg.nacks", "exchange", config.exchange, "queue", "elysium2-frontend-enriched");
        produceMsgExceptions = meterRegistry.counter("produceMsg.exceptions", "exchange", config.exchange, "queue", "elysium2-frontend-enriched");
        setupCallbacks();
    }

    private void setupCallbacks() {
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
//                logger.info("Received " + (ack ? " ack " : " nack ") + "for correlation: " + correlationData);
                if (ack) {
                    producMsgAcks.increment();
                } else {
                    produceMsgNacks.increment();
                }
            }
        });
//        this.rabbitTemplate.setReturnsCallback(returned -> {
//            logger.info("Returned: " + returned.getMessage() + "\nreplyCode: " + returned.getReplyCode()
//                    + "\nreplyText: " + returned.getReplyText() + "\nexchange/rk: "
//                    + returned.getExchange() + "/" + returned.getRoutingKey());
//        });
    }

    public void send(String msg) {
        try {
            CorrelationData correlationData = new CorrelationData(String.format("Correlation for msg [%s]", msg));
            rabbitTemplate.convertAndSend(config.exchange, "com.thg.msging", msg, correlationData);
            produceMsgAttempts.increment();
//                CorrelationData.Confirm confirm = correlationData.getFuture().get(100, TimeUnit.MILLISECONDS);
//                if (confirm != null) {
//                    if (confirm.isAck()) {
//                        producMsgAcks.increment();
//                    } else {
//                        produceMsgNacks.increment();
//                    }
//                }
        } catch (Exception e) {
            logger.info("Exception sending message: ", e.getMessage());
            produceMsgExceptions.increment();
        }
    }
}
