package com.zxin.order.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ的接收消息的类
 */
@Slf4j
@Component
public class MQReceiver {

    // 1、@RabbitListener(queues = "myQueue") 需要自己新建Queue

    // 2、自动创建队列 @RabbitListener(queuesToDeclare = @Queue("myQueue"))

    // 3、自动创建队列，并且可以支持和exchange实现绑定
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("myQueue"),
            exchange = @Exchange("myExchange")
    ))
    public void process(String message){
        log.info("MqReceiver: {}", message);
    }

    /**
     * 虽然上面只用到了一个 ，但是实际可能需要一个队列对应多个服务，比如下面两个服务演示
     * myOrder这一个消息队列，对应两个Exchange，也就是两个服务
     * 比如数码供应商，水果供应商
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("myOrder"),
            key = "computer", // 路由key ,这个就是 Exchange和Queue之间的绑定关系
            exchange = @Exchange("myOrderExchange")
    ))
    public void processComputer(String message){
        log.info("computer MqReceiver: {}", message);
    }

    /**
     * 虽然上面只用到了一个 ，但是实际可能需要多个服务
     * 比如数码供应商，水果供应商
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("myOrder"),
            key = "fruit", // 路由key ,这个就是 Exchange和Queue之间的绑定关系
            exchange = @Exchange("myFruitExchange")
    ))
    public void processFruit(String message){
        log.info("fruit MqReceiver: {}", message);
    }
}
