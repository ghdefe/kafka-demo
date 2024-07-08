package com.example.producer.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MsgProducer {

    private static final Logger log = LoggerFactory.getLogger(MsgProducer.class);
    private static final AtomicInteger count = new AtomicInteger(0);
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    /**
     * <p>根据文档：{@linkplain DefaultPartitioner}，有三种分区策略: </p>
     * <ul>
     * <li>If a partition is specified in the record, use it —— 指定分区策略</li>
     * <li>If no partition is specified but a key is present choose a partition based on a hash of the key ——  哈希策略</li>
     * <li>If no partition or key is present choose the sticky partition that changes when the batch is full —— 黏性分区策略</li>
     * </ul>
     * <p>以下未指定键且未指定分区，因此使用黏性分区策略，注意不是轮询策略。也就是消息会一直发送到同一个分区，直至该分区批次满，再一直发送到另一个分区，因此测试均衡效果需要发送消息条数多一些</p>
     *
     */
    public void send(String topic, String msg) {
        int i = count.incrementAndGet();
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, msg);


        SendResult<String, String> result;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        ProducerRecord<String, String> record = result.getProducerRecord();
        RecordMetadata recordMetadata = result.getRecordMetadata();

        log.info("主题：{}, 消息：{}, 分区:{}, 偏移量：{}, 键: {}",
                record.topic(), record.value(), recordMetadata.partition(), recordMetadata.offset(), record.key());
    }

}
