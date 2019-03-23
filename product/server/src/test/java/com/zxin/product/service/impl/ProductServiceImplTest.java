package com.zxin.product.service.impl;

import com.zxin.product.common.DecreaseStockInput;
import com.zxin.product.common.ProductInfoOutput;
import com.zxin.product.dataobject.ProductInfo;
import com.zxin.product.dto.CartDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;


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
        List<ProductInfoOutput> list = productService.findList(Arrays.asList("157875196366160022", "164103465734242707"));
        for(ProductInfoOutput product : list){
            System.out.println(product);
        }
    }

    // List<DecreaseStockInput>
    @Test
    public void decreaseStock() throws Exception{
        DecreaseStockInput dsInput = new DecreaseStockInput("157875196366160022", 2);// 对这个商品扣两件
        productService.decreaseStock(Arrays.asList(dsInput));
    }

}