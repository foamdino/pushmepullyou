package com.thg.msging;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQProducer {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final Config config;

    private Counter produceMsgAttempts;
    private Counter producMsgAcks;
    private Counter produceMsgNacks;
    private Counter produceMsgExceptions;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate, Config config, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
        produceMsgAttempts = meterRegistry.counter("produceMsg.attempts", "exchange", config.exchange, "queue", config.qName);
        producMsgAcks = meterRegistry.counter("produceMsg.acks", "exchange", config.exchange, "queue", config.qName);
        produceMsgNacks = meterRegistry.counter("produceMsg.nacks", "exchange", config.exchange, "queue", config.qName);
        produceMsgExceptions = meterRegistry.counter("produceMsg.exceptions", "exchange", config.exchange, "queue", config.qName);
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
