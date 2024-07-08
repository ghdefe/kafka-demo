package com.example.producer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MsgProducerTest {

    @Autowired
    MsgProducer msgProducer;
    @Test
    void send() {
        msgProducer.send("single-topic", "hello");
    }

    @Test
    void sendMulti() {
        for (int i = 0; i < 1000; i++) {
            msgProducer.send("multi-topic", "world" + i);
        }
    }
}