package com.zxin.order.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StreamReceiverTest {

    @Autowired
    private StreamClient streamClient;

    @Test
    public void process() throws Exception {
        String message = "now " + new Date();
        // 发送消息
        streamClient.output().send(MessageBuilder.withPayload(message).build());
    }
}