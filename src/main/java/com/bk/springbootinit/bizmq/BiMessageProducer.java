package com.bk.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
/**
 * 使用自动化软件创建的，类似于redis
 * @author bk
 * @data 2023/11/11 10:50
*/
@Component
public class BiMessageProducer
{
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sentMessage(String msg)
    {
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME,
                BiMqConstant.BI_ROUTING_KEY,msg);
    }

}
