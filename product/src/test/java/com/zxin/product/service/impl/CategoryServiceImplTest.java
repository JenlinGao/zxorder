package com.zxin.product.service.impl;

import com.zxin.product.dataobject.ProductCategory;
import com.zxin.product.service.CategoryService;
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
public class CategoryServiceImplTest {


    @Autowired
    private CategoryService categoryService;

    @Test
    public void findByCategoryTypeIn() throws Exception {

        List<ProductCategory> byCategoryTypeIns = categoryService.findByCategoryTypeIn(Arrays.asList(11, 22));

        for(ProductCategory byCategoryTypeIn : byCategoryTypeIns)
            System.out.println(byCategoryTypeIn);

    }

}