package com.bk.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * 这个是接受的，我们这回不是声明队列（消费者和生产者），而是由交换即知道队列即可
 *
 * @author bk
 * @data 2023/11/10 21:14
 */

public class FanoutConsumer {
    private static final String EXCHANGE_NAME = "fanout-exchange";


    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
//        其实chnnal
        Connection connection = connectionFactory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
//        声明交换机
//            我好想没有声明队列

        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        声明一个称号
        String queueName = "xiaowang_queue";
        channel1.queueDeclare(queueName,true,false,false,null);

//        然后绑定，不然别人怎么知道是你
        channel1.queueBind(queueName, EXCHANGE_NAME, "");

        String queueName2 = "xiaoli_queue";
        channel2.queueDeclare(queueName2, true, false, false, null);
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小王] Received '" + message + "'");
        };

        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小李] Received '" + message + "'");
        };
        channel1.basicConsume(queueName, true, deliverCallback1, consumerTag -> {
        });
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });


    }
//    public static void main(String[] argv) throws Exception {
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        connectionFactory.setHost("106.55.59.85");
//        connectionFactory.setUsername("guest");
//        connectionFactory.setPassword("guest");
//        connectionFactory.setPort(5672);
//        Connection connection = connectionFactory.newConnection();
//        Channel channel1 = connection.createChannel();
//        Channel channel2 = connection.createChannel();
//        // 声明交换机
//        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        // 创建队列，随机分配一个队列名称
//        String queueName = "xiaowang_queue";
//        channel1.queueDeclare(queueName, true, false, false, null);
//        channel1.queueBind(queueName, EXCHANGE_NAME, "");
//
//        String queueName2 = "xiaoli_queue";
//        channel2.queueDeclare(queueName2, true, false, false, null);
//        channel2.queueBind(queueName2, EXCHANGE_NAME, "");
//
//        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
//
//        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [小王] Received '" + message + "'");
//        };
//
//        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
//            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [小李] Received '" + message + "'");
//        };
//        channel1.basicConsume(queueName, true, deliverCallback1, consumerTag -> {
//        });
//        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
//        });
//    }

}
