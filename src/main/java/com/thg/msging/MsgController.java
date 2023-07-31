package com.thg.msging;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MsgController {

    private final RabbitMQProducer producer;

    @Autowired
    public MsgController(RabbitMQProducer producer) {
        this.producer = producer;
    }

    @PostMapping(path="/producemsgs/{numberOfMessages}", produces={"application/json"})
    public int produceMessages(int numberOfMessages) {
        for (int i = 0; i < numberOfMessages; i++) {
            producer.send(String.format("msg-%d", i));
        }

        return numberOfMessages;
    }
}
