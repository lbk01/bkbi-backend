package com.bk.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
/**
 * 一对多的生产者和消费者关系，这里是按照顺序来接收消息的
 * @author bk
 * @data 2023/11/10 21:08
*/
public class MultiConsumer
{
    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        Connection connection = connectionFactory.newConnection();
//        这里是使用循环来模拟两个消费者看同一个队列


        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();

            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            channel.basicQos(1);

            // 定义了如何处理消息
            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");

                try {
                    // 处理工作
                    System.out.println(" [x] Received '" + "编号:" + finalI + ":" + message + "'");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    // 停 20 秒，模拟机器处理能力有限
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            // 开启消费监听
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }



    }
}
