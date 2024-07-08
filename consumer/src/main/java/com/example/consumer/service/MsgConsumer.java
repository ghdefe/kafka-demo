package com.example.consumer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MsgConsumer {

    private static final Logger log = LoggerFactory.getLogger(MsgConsumer.class);


    @KafkaListener(topics = "single-topic", groupId = "consumer-group1")
    public void consume(String topic, String msg) {
        log.info("主题: {}, 消息: {}", "single-topic", msg);
    }

    /**
     * 这里是类似广播的效果
     * @param topic
     * @param msg
     */
    @KafkaListener(topics = "single-topic", groupId = "consumer-group2")
    public void consume2(String topic, String msg) {
        log.info("同一条消息被不同消费者组消费: 主题: {}, 消息: {}", "single-topic", msg);
    }

    /**
     * 这里测试消息均衡分配效果
     * @param msg
     */
    @KafkaListener(topics = "multi-topic", groupId = "consumer-group2")
    public void consumeMulti(String msg) {
        log.info("多条消息均衡分配分区, 主题: {}, 消息: {}", "multi-topic", msg);
    }

}
