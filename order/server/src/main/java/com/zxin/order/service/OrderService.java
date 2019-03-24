package com.zxin.order.service;

import com.zxin.order.dto.OrderDTO;

public interface OrderService {

    // 这个就是创建订单
    OrderDTO create(OrderDTO orderDTO);


    // 完结订单(只能卖家操作)
    OrderDTO finish(String orderId);
}
