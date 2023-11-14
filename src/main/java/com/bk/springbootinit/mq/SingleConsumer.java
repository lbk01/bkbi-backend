package com.bk.springbootinit.mq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class SingleConsumer
{
//    接受的队列
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) {

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // 创建队列，这里队列的参数需要保持一致
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            // 定义了如何处理消息
            DeliverCallback deliverCallback = (Consumer,deliverCallback1)->
            {
                String message = new String(deliverCallback1.getBody(), StandardCharsets.UTF_8);
                System.out.println(" [x] Received '" + message + "'");
            };
//
            channel.basicConsume(QUEUE_NAME,true,deliverCallback,Consumer->{});
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
//        这里来一个阻塞队列持续监听消息



    }
}
