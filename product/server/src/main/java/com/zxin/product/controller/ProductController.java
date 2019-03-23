package com.zxin.product.controller;


import com.zxin.product.common.DecreaseStockInput;
import com.zxin.product.common.ProductInfoOutput;
import com.zxin.product.dataobject.ProductCategory;
import com.zxin.product.dataobject.ProductInfo;
import com.zxin.product.dto.CartDTO;
import com.zxin.product.service.CategoryService;
import com.zxin.product.service.ProductService;
import com.zxin.product.utils.ResultVOUtil;
import com.zxin.product.viewobject.ProductInfoVO;
import com.zxin.product.viewobject.ProductVO;
import com.zxin.product.viewobject.ResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    /**
     * 1、查询所有在架的商品
     * 2、获取类目type列表
     * 3、查询类目
     * 4、构造数据
     */
    @GetMapping("/list")
    public ResultVO<ProductVO> list(){
        // 1、查询所有在架的商品
        List<ProductInfo> productInfoList = productService.findUpAll();

        // 2、获取类目type列表 (所有商品的类目列表)
        List<Integer> categoryTypeList = productInfoList.stream()
                .map(ProductInfo::getCategoryType)
                .collect(Collectors.toList());

        // 3、从数据库查询类目 -- > 查询到所有类目
        List<ProductCategory> categoryList = categoryService.findByCategoryTypeIn(categoryTypeList);

        // 4、构造数据
        List<ProductVO> productVOList = new ArrayList<>(); //第二层的商品
        for(ProductCategory productCategory : categoryList){ // 遍历类目
            ProductVO productVo = new ProductVO();//外层的一个
            productVo.setCategoryName(productCategory.getCategoryName());
            productVo.setCategoryType(productCategory.getCategoryType());

            // 里面又是一个list -> 商品list
            List<ProductInfoVO> productInfoVoList = new ArrayList<>();
            for(ProductInfo productInfo : productInfoList){
                if(productInfo.getCategoryType().equals(productCategory.getCategoryType())) {

                    ProductInfoVO productInfoVO = new ProductInfoVO();
                    // source, target, 将source的属性拷贝的target中，省略了5个setter方法
                    BeanUtils.copyProperties(productInfo, productInfoVO);
                    productInfoVoList.add(productInfoVO);
                }
            }

            productVo.setProductInfoVOList(productInfoVoList);
            productVOList.add(productVo);
        }

        // 最外层的 --> 返回
        return ResultVOUtil.success(productVOList); //包装成返回的工具类
    }

    /**
     * 获取商品列表(专门给订单服务用的)
     *
     * 注意这里的@RequestBody的用法 参考: https://blog.csdn.net/justry_deng/article/details/80972817
     * @param productIdList
     * @return
     */
//    @GetMapping("/listForOrder") // 切记不要用 GetMapping
    @PostMapping("/listForOrder")
    public List<ProductInfoOutput> listForOrder(@RequestBody List<String>productIdList){ // 注意这里的@RequestBody的用法
        return productService.findList(productIdList);
    }

    // 扣库存(供 order 调用)
    @PostMapping("/decreaseStock")
    public void decreaseStock(@RequestBody List<DecreaseStockInput> decreaseStockInputList) {
        productService.decreaseStock(decreaseStockInputList);
    }
}
