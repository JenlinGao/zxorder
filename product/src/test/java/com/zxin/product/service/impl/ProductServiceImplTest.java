package com.zxin.product.service.impl;

import com.zxin.product.dataobject.ProductInfo;
import com.zxin.product.dto.CartDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductServiceImplTest {


    @Autowired
    private ProductServiceImpl productService;

    @Test
    public void findUpAll() throws Exception {

        List<ProductInfo> upAll = productService.findUpAll();

        for(ProductInfo upProduct : upAll){
            System.out.println(upProduct);
        }
    }

    @Test
    public void findList() throws Exception{
        List<ProductInfo> list = productService.findList(Arrays.asList("157875196366160022", "164103465734242707"));
        for(ProductInfo product : list){
            System.out.println(product);
        }
    }

    @Test
    public void decreaseStock() throws Exception{
        CartDTO cartDTO = new CartDTO("157875196366160022", 2);// 对这个商品扣两件
        productService.decreaseStock(Arrays.asList(cartDTO));
    }

}