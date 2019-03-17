package com.zxin.order.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.zxin.order.dataobject.OrderDetail;
import com.zxin.order.dto.OrderDTO;
import com.zxin.order.enums.ResultEnum;
import com.zxin.order.exception.OrderException;
import com.zxin.order.form.OrderForm;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class OrderForm2OrderDTOConverter {

    public static OrderDTO convert(OrderForm orderForm){
        Gson gson = new Gson();
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName(orderForm.getName());
        orderDTO.setBuyerPhone(orderForm.getPhone());
        orderDTO.setBuyerAddress(orderForm.getAddress());
        orderDTO.setBuyerOpenid(orderForm.getOpenid());

        List<OrderDetail> orderDetailList = new ArrayList<>(); //保存的
        try {
            orderDetailList = gson.fromJson(orderForm.getItems(),
                    new TypeToken<List<OrderDetail>>() {
                    }.getType());
        } catch (Exception e) {
            log.error("【json转换】错误, string={}", orderForm.getItems());
            throw new OrderException(ResultEnum.PARAM_ERROR);  //参数错误
        }

        orderDTO.setOrderDetailList(orderDetailList);
        return orderDTO;
    }
}
