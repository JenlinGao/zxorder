package com.zxin.product.repository;

import com.zxin.product.dataobject.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


// 第一个参数是实体，第二个参数是主键的类型
public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {

    List<ProductInfo> findByProductStatus(Integer productStatus);


    // 后来为订单服务重新写的
    List<ProductInfo> findByProductIdIn(List<String> productIdList);
}
