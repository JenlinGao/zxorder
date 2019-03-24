package com.zxin.order.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;


/**
 * 发送MQ消息测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MQReceiverTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void send() throws Exception {
        amqpTemplate.convertAndSend("myQueue", "now" + new Date());
    }

    // 订单服务下单之后要发送消息
    @Test
    public void sendOrder() throws Exception {
        // exchange, routingKey，message
        amqpTemplate.convertAndSend("myOrder","computer", "now" + new Date());
    }
}