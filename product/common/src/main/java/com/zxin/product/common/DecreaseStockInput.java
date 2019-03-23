package com.zxin.product.common;

import lombok.Data;

/**
 * 减库存入参
 * 这个类 和 CartDTO其实是一样的，但是为了封装不暴露给外界，所以这里使用了一个相同的类
 */
@Data
public class DecreaseStockInput {

    private String productId;

    private Integer productQuantity;

    public DecreaseStockInput() {
    }

    public DecreaseStockInput(String productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}