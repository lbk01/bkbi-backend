package com.bk.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

/**
 * 这个是具有交换机形式的广播模式，一条消息给多个消费者知道
 * 就像我们平时听的广播一样
 * @author bk
 * @data 2023/11/10 21:10
*/
public class FanoutProducer
{

    private static final String EXCHANGE_NAME = "fanout-exchange";
    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // 创建交换机
            channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }

    }
}
