package com.zxin.order.client;


import com.zxin.order.dataobject.ProductInfo;
import com.zxin.order.dto.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// Feign的配置  底层使用的是动态代理 , 远程调用
@FeignClient(value = "product")
public interface ProductClient {

//    @GetMapping("/product/listForOrder") // 切记切记，不能用GetMapping，因为RequestBody不能用GetMapping
    @PostMapping("/product/listForOrder")  // 注意不要忘记product前缀
    List<ProductInfo> listForOrder(@RequestBody List<String>productIdList); // 用RequestParam就可以用GetMapping


    @PostMapping("/product/decreaseStock")
    void decreaseStock(@RequestBody List<CartDTO> cartDTOList);
}