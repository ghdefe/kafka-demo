

## kafka消息发送接收示例

### 1. 启动kafka实例

```shell
cat << EOF > docker-compose.yml
version: "2"

services:
  zookeeper:
    image: docker.io/bitnami/zookeeper:3.8
    ports:
      - "2181:2181"
      - "2180:8080"
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
  kafka:
    image: docker.io/bitnami/kafka:3.2
    ports:
      - "9092:9092"
    environment:
      - KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181
      - ALLOW_PLAINTEXT_LISTENER=yes
      - KAFKA_CFG_MAX_REQUEST_SIZE=1195725856
      - KAFKA_CFG_LISTENERS=PLAINTEXT://:9092
      - KAFKA_CFG_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
    depends_on:
      - zookeeper
EOF

docker-compose up -d
```

### 2. 启动消费者

消费者主要代码: `com/example/consumer/service/MsgConsumer.java`, 消费者启动类: `com/example/consumer/ConsumerApplication.java`.  

测试消息均衡分配效果需要启动多个消费者实例,因此在IntelliJ Idea中右上角, 启动配置中配置`Allow multiple instances`, 就可以多次点击启动按钮启动多个消费者实例.

### 3. 启动生产者

生产者配置: `com/example/producer/config/TopicConfiguration.java`, 在这里定义了`multi-topic`主题的分区数为4,用于测试消息均衡效果.  

生产消息: 运行此类可发送1条消息到`single-topic`和1000条消息到`multi-topic`. `com/example/producer/service/MsgProducerTest.java`



### 笔记

消费者组中的成员会自动被分配分区, 消费组中的成员`自动`且`均衡`地分配所有的分区. 例如:  
`multi-topic`有4个分区.  

- 启动第一个消费者实例时, 会打印以下内容:    

```log
2024-07-08T15:46:19.578+08:00  INFO 21836 --- [consumer] [ntainer#2-0-C-1] k.c.c.i.ConsumerRebalanceListenerInvoker : [Consumer clientId=consumer-consumer-0, groupId=consumer-group2] Adding newly assigned partitions: multi-topic-0, multi-topic-1, multi-topic-2, multi-topic-3
2024-07-08T15:46:19.604+08:00  INFO 21836 --- [consumer] [ntainer#2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consumer-group2: partitions assigned: [multi-topic-0, multi-topic-1, multi-topic-2, multi-topic-3]
```  
  
可见`consumer-group2`分配了4个分区: `multi-topic-0, multi-topic-1, multi-topic-2, multi-topic-3`.  

- 接着启动第二个消费者实例
> 第一个消费者打印日志
```log
2024-07-08T15:51:42.526+08:00  INFO 21836 --- [consumer] [ntainer#2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consumer-group2: partitions revoked: [multi-topic-0, multi-topic-1, multi-topic-2, multi-topic-3]
2024-07-08T15:51:42.602+08:00  INFO 21836 --- [consumer] [ntainer#2-0-C-1] k.c.c.i.ConsumerRebalanceListenerInvoker : [Consumer clientId=consumer-consumer-0, groupId=consumer-group2] Adding newly assigned partitions: multi-topic-0, multi-topic-1
2024-07-08T15:51:42.607+08:00  INFO 21836 --- [consumer] [ntainer#2-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consumer-group2: partitions assigned: [multi-topic-0, multi-topic-1]
```

> 第二个消费者打印日志
```log
2024-07-08T15:51:42.602+08:00  INFO 21276 --- [consumer] [ntainer#1-0-C-1] k.c.c.i.ConsumerRebalanceListenerInvoker : [Consumer clientId=consumer-consumer-0, groupId=consumer-group2] Adding newly assigned partitions: multi-topic-2, multi-topic-3
2024-07-08T15:51:42.612+08:00  INFO 21276 --- [consumer] [ntainer#1-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : consumer-group2: partitions assigned: [multi-topic-2, multi-topic-3]
```
  
可见在第二个消费者启动时, 会触发分区重新分配
- 第一个消费者先撤销现有4个分区分配, 然后重新获取分配的分区, 最终绑定分区: multi-topic-0, multi-topic-1  
- 第二个消费者获取分配的分区, 最终绑定分区: multi-topic-2, multi-topic-3  
可见: 消费组中的成员`自动`且`均衡`地分配所有的分区.