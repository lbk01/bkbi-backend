package com.bk.springbootinit.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 这个是最简单一对一的消息队列
 * @author bk
 * @data 2023/11/10 20:15
*/
public class SingleProducer
{
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] args) {
//        创建连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("106.55.59.85");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setPort(5672);
        connectionFactory.setHandshakeTimeout(300000000);//设置握手超时时间
        try {
//            获得一个连接
//            信道，多路复用连接中的一条独立的双向数据流通道。
//            信道是建立在真实的TCP连接内地虚拟连接，AMQP 命令都是通过信道发出去的，
//            不管是发布消息、订阅队列还是接收消息，这些动作都是通过信道完成。
//            因为对于操作系统来说建立和销毁 TCP 都是非常昂贵的开销，
//            所以引入了信道的概念，以复用一条 TCP 连接。
            Connection connection = connectionFactory.newConnection();
//然后创建消息队列，声明
            Channel channel = connection.createChannel();
//            这几个参数分别是持久化独家的，自动删除，一些绑定的配置
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);

//            发送消息
            String message = "Hello World!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

    }
}
