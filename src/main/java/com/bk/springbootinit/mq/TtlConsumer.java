package com.bk.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TtlConsumer
{
    private final static String QUEUE_NAME = "ttl_queue";

    public static void main(String[] argks) throws Exception{
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        HashMap<String, Object> args = new HashMap<>();
//        给队列的时候注意一下就好,指定一下配置
        args.put("x-message-ttl",5000);
        channel.exchangeDeclare("my-exchange","fanout");
        channel.queueDeclare(QUEUE_NAME,false,false,false,args);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        // 定义了如何处理消息

        channel.queueBind(QUEUE_NAME,"my-exchange","routing-key");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        // 消费消息，会持续阻塞
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });



    }
}
