package com.bk.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 用于创建测试程序用到的交换机和队列（只用在程序启动前执行一次）
 * @author bk
 * @data 2023/11/11 10:39
*/
public class BiInitMain
{
    public static void main(String[] args) {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("106.55.59.85");
            connectionFactory.setUsername("guest");
            connectionFactory.setPassword("guest");
            connectionFactory.setPort(5672);
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME =  BiMqConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 创建队列，随机分配一个队列名称
            String queueName = BiMqConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME,  BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {

        }


    }
}
