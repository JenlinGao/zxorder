package com.zxin.order.service;

import com.zxin.order.dto.OrderDTO;

public interface OrderService {

    // 这个就是创建订单
    OrderDTO create(OrderDTO orderDTO);

}
