package com.zxin.order.form;


import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * 前端API传递过来的参数
 */
@Data
public class OrderForm {

    // 传递过来的参数, 注意这里有表单验证
    /**
     * 买家姓名
     */
    @NotEmpty(message = "姓名必填")
    private String name;

    /**
     * 买家手机号
     */
    @NotEmpty(message = "手机号必填")
    private String phone;

    /**
     * 买家地址
     */
    @NotEmpty(message = "地址必填")
    private String address;

    /**
     * 买家微信openid
     */
    @NotEmpty(message = "openid必填")
    private String openid;

    /**
     * 购物车, 这里本来传递给我的是一个String ，但是也是一个字符串 ，先拿到一个字符串，然后再做处理
     */
    @NotEmpty(message = "购物车不能为空")
    private String items;
}
