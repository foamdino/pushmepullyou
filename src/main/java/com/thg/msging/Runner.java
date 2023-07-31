package com.thg.msging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Runner implements CommandLineRunner {


    private final RabbitMQProducer producer;
    private final Config config;

    @Autowired
    public Runner(RabbitMQProducer producer, Config config) {
        this.producer = producer;
        this.config = config;
    }

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < config.messages; i++) {
            producer.send(String.format("msg-%d", i));
        }
    }
}
