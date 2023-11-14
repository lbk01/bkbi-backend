package com.bk.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * 这个是mq的消息过期机制,注意这个
 *
 * @author bk
 * @data 2023/11/10 22:50
 */
public class TtlProducer {
    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
//        其实chnnal

        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // 发送消息
            String message = "Hello World!";
            AMQP.BasicProperties  properties= new AMQP.BasicProperties().builder()
                    .expiration("1000")
                    .build();
            channel.basicPublish("my-exchange", "routing-key", properties, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");

        }
    }
}
