package com.zxin.product.repository;

import com.zxin.product.dataobject.ProductInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductInfoRepositoryTest {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Test
    public void findByProductStatus() throws Exception {
        List<ProductInfo> productInfos = productInfoRepository.findByProductStatus(0);
        for(ProductInfo productInfo : productInfos){
            System.out.println(productInfo);
        }
    }

    @Test
    public void findByProductIdIn() throws Exception{
        // 查两个商品
        List<ProductInfo> productInfosById = productInfoRepository.findByProductIdIn(Arrays.asList("157875196366160022", "164103465734242707"));
        for(ProductInfo product : productInfosById){
            System.out.println(product);
        }
    }
}