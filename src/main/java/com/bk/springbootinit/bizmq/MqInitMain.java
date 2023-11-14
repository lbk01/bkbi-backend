package com.bk.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MqInitMain {

    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("106.55.59.85");
            connectionFactory.setUsername("guest");
            connectionFactory.setPassword("guest");
            connectionFactory.setPort(5672);
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = "code_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, "my_routingKey");
        } catch (Exception e) {

        }

    }
}
