package com.zxin.order.repository;

import com.zxin.order.dataobject.OrderMaster;
import com.zxin.order.enums.OrderStatusEnum;
import com.zxin.order.enums.PayStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;


@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderMasterRepositoryTest {



    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Test
    public void testSave(){
        OrderMaster orderMaster = new OrderMaster();

        orderMaster.setOrderId("1234567");
        orderMaster.setBuyerName("zxzxin");
        orderMaster.setBuyerPhone("18812311231");
        orderMaster.setBuyerAddress("长沙天心区");
        orderMaster.setBuyerOpenid("1101110"); // 微信号 OpenId
        orderMaster.setOrderAmount(new BigDecimal(3)); // 点的数量
        orderMaster.setOrderStatus(OrderStatusEnum.NEW.getCode()); // 新订单
        orderMaster.setPayStatus(PayStatusEnum.WAIT.getCode()); // 默认等待支付
        // 时间不需要设置, 数据库默认设计好了

        // 调用save()方法，返回的也是OrderMaster对象，只要返回的不是null，就是代表成功了
        OrderMaster ret = orderMasterRepository.save(orderMaster);
        Assert.assertTrue(ret != null);

    }

}