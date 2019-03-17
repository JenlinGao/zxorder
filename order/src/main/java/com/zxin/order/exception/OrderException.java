package com.zxin.order.exception;

import com.zxin.order.enums.ResultEnum;

/**
 * 订单异常
 */
public class OrderException extends RuntimeException{

    private Integer code; //异常类型

    public OrderException(Integer code, String message){
        super(message);
        this.code = code;
    }

    public OrderException(ResultEnum resultEnum){
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }
}
