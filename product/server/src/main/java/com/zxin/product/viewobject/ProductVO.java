package com.zxin.product.viewobject;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// http请求返回的第二层对象 (就是类目)
@Data
public class ProductVO {

    @JsonProperty("name")  // 因为返回给前段又必须是 name, 所以加上这个注解
    private String categoryName;

    @JsonProperty("type")
    private Integer categoryType;

    @JsonProperty("foods")
    List<ProductInfoVO> productInfoVOList;
}
