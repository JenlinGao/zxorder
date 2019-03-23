package com.zxin.order.viewobject;

import lombok.Data;

// 返回给前端的结果
@Data
public class ResultVO<T> {

    private Integer code;

    private String msg;

    private T data;
}
