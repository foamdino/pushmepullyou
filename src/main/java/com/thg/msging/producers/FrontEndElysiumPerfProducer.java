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

import java.util.concurrent.TimeUnit;

@Component
public class FrontEndElysiumPerfProducer {

    private final Logger logger = LoggerFactory.getLogger(FrontEndElysiumPerfProducer.class);
    private final RabbitTemplate rabbitTemplate;
    private final Config config;
    private Counter produceMsgAttempts;
    private Counter producMsgAcks;
    private Counter produceMsgNacks;
    private Counter produceMsgExceptions;

    @Autowired
    public FrontEndElysiumPerfProducer(@Qualifier("frontend-elysium-perf-data-template") RabbitTemplate rabbitTemplate, Config config, MeterRegistry meterRegistry) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
        produceMsgAttempts = meterRegistry.counter("produceMsg.attempts", "exchange", config.exchange, "queue", "frontend-elysium-perf-data");
        producMsgAcks = meterRegistry.counter("produceMsg.acks", "exchange", config.exchange, "queue", "frontend-elysium-perf-data");
        produceMsgNacks = meterRegistry.counter("produceMsg.nacks", "exchange", config.exchange, "queue", "frontend-elysium-perf-data");
        produceMsgExceptions = meterRegistry.counter("produceMsg.exceptions", "exchange", config.exchange, "queue", "frontend-elysium-perf-data");
        setupCallbacks();
    }

    private void setupCallbacks() {
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (correlationData != null) {
                try {
                    CorrelationData.Confirm confirm = correlationData.getFuture().get(500, TimeUnit.MILLISECONDS);
                    if (confirm.isAck()) {
                        producMsgAcks.increment();
                    } else {
                        produceMsgNacks.increment();
                        logger.debug(String.format("Nack returned: %s", cause));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    produceMsgExceptions.increment();
                }
            }

//            if (ack) {
//                producMsgAcks.increment();
//            } else {
//                produceMsgNacks.increment();
//                logger.debug(String.format("Nack returned: %s", cause));
//            }
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
            rabbitTemplate.convertAndSend(config.exchange, "com.thg.frontend-elysium-perf-data-q", msg, correlationData);
            produceMsgAttempts.increment();
        } catch (Exception e) {
            logger.info(String.format("Exception sending message: %s", e.getMessage()));
            produceMsgExceptions.increment();
        }
    }
}
