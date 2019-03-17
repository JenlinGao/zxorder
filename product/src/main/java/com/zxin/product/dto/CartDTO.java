package com.zxin.product.dto;


import lombok.Data;


// 订单服务 调用商品服务 扣库存的 传入的 json的一个数据转换 dto
@Data
public class CartDTO {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品数量
     */
    private Integer productQuantity;


    public CartDTO() {
    }

    public CartDTO(String productId, Integer productQuantity) {
        this.productId = productId;
        this.productQuantity = productQuantity;
    }
}
