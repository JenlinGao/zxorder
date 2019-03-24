package com.zxin.product.service.impl;


import com.zxin.product.common.DecreaseStockInput;
import com.zxin.product.common.ProductInfoOutput;
import com.zxin.product.dataobject.ProductInfo;
import com.zxin.product.dto.CartDTO;
import com.zxin.product.enums.ProductStatusEnum;
import com.zxin.product.enums.ResultEnum;
import com.zxin.product.exception.ProductException;
import com.zxin.product.repository.ProductInfoRepository;
import com.zxin.product.service.ProductService;
import com.zxin.product.utils.JsonUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService{

    // Dao层注入到Service层
    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Override
    public List<ProductInfo> findUpAll() {
        return productInfoRepository.findByProductStatus(ProductStatusEnum.UP.getCode()); // 枚举, 在架的状态
    }

    //发送MQ消息
    @Autowired
    private AmqpTemplate amqpTemplate;

    // 后来加的, 再后来改成了多模块
    @Override
    public List<ProductInfoOutput> findList(List<String> productIdList) {
        // 改写之前的代码
//        return productInfoRepository.findByProductIdIn(productIdList);
        // 多模块之后的, 将List<ProductInfo>转换成 ProductInfoOutput
        return productInfoRepository.findByProductIdIn(productIdList).stream()
                .map(e -> {
                    ProductInfoOutput output = new ProductInfoOutput();
                    BeanUtils.copyProperties(e, output);
                    return output;
                })
                .collect(Collectors.toList());
    }

    // 后来加的， 扣库存的
    @Override
    public void decreaseStock(List<DecreaseStockInput> decreaseStockInputList) {

        // 改写之前代码
//
//        for(CartDTO cartDTO : cartDTOList){
//            Optional<ProductInfo> productInfoOptional = productInfoRepository.findById(cartDTO.getProductId());
//            //判断商品是否存在
//            if(!productInfoOptional.isPresent()){ //如果商品不存在 ,抛出一个异常
//                throw new ProductException(ResultEnum.PRODUCT_NOT_EXIST);
//            }
//            // 如果商品存在，还需要判断一下数量
//            ProductInfo productInfo = productInfoOptional.get();
//            Integer result = productInfo.getProductStock() - cartDTO.getProductQuantity();
//            if(result < 0) { //库存不够
//                throw new ProductException(ResultEnum.PRODUCT_STOCK_ERROR);
//            }
//
//            productInfo.setProductStock(result);
//            /**
//             * 网上看到一个问题, 但我这里应该没有问题.
//             * JpaRepository的save(product)方法做更新操作,更新商品的库存和价格，所以入参的product只
//              设置了商品的库存和价格，结果调用完save方法后，除了库存和价格的数据变了，其他字段全部被更新成了null
//             */
//            productInfoRepository.save(productInfo); // 保存更新
//        }

        List<ProductInfo> productInfoList = decreaseStockProcess(decreaseStockInputList);

        //发送mq消息
        List<ProductInfoOutput> productInfoOutputList = productInfoList.stream().map(e -> {
            ProductInfoOutput output = new ProductInfoOutput();
            BeanUtils.copyProperties(e, output);
            return output;
        }).collect(Collectors.toList());

        //注意要在外面发送
        // 发送MQ消息
        amqpTemplate.convertAndSend("productInfo", JsonUtil.toJson(productInfoOutputList));

    }


    @Transactional
    public List<ProductInfo> decreaseStockProcess(List<DecreaseStockInput> decreaseStockInputList) {
        List<ProductInfo> productInfoList = new ArrayList<>();

        for (DecreaseStockInput decreaseStockInput: decreaseStockInputList) {
            Optional<ProductInfo> productInfoOptional = productInfoRepository.findById(decreaseStockInput.getProductId());
            //判断商品是否存在
            if (!productInfoOptional.isPresent()){
                throw new ProductException(ResultEnum.PRODUCT_NOT_EXIST);
            }

            ProductInfo productInfo = productInfoOptional.get();
            //库存是否足够
            Integer result = productInfo.getProductStock() - decreaseStockInput.getProductQuantity();
            if (result < 0) {
                throw new ProductException(ResultEnum.PRODUCT_STOCK_ERROR);
            }

            productInfo.setProductStock(result);
            productInfoRepository.save(productInfo);

            productInfoList.add(productInfo);
        }
        return productInfoList;
    }
}
