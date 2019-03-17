package com.zxin.order.controller;

import com.zxin.order.converter.OrderForm2OrderDTOConverter;
import com.zxin.order.dto.OrderDTO;
import com.zxin.order.enums.ResultEnum;
import com.zxin.order.exception.OrderException;
import com.zxin.order.form.OrderForm;
import com.zxin.order.service.OrderService;
import com.zxin.order.utils.ResultVOUtil;
import com.zxin.order.viewobject.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 下单步骤
       1、参数校验；
       2、查询商品信息 (需要调用远程服务 Feign；
       3、计算总价；
       4、扣库存(调用商品服务)；
       5、订单入库；
     */

    // 这里参数很多，所以构造一个对象
    // 校验，这里校验不清楚的可以看这篇博客: https://www.cnblogs.com/cjsblog/p/8946768.html
    @PostMapping("/create")
    public ResultVO<Map<String, String>> create(@Valid OrderForm orderForm,
                                                BindingResult bindingResult){

        // 参数绑定有误
        if(bindingResult.hasErrors()) {
            log.error("【创建订单】参数不正确, orderForm={}", orderForm);
            throw new OrderException(ResultEnum.PARAM_ERROR.getCode(),
                    bindingResult.getFieldError().getDefaultMessage()); // 获取到 @NotEmpty(message = "姓名必填")的错误信息
        }

        // orderForm -> orderDTO  将ordrForm转换成orderDTO，这样就可以调用那个service了
        // 调用转换器
        OrderDTO orderDTO = OrderForm2OrderDTOConverter.convert(orderForm);

        //还需要再判断一次是不是为空
        if(CollectionUtils.isEmpty(orderDTO.getOrderDetailList())){
            log.error("【创建订单】购物车为空!");
            throw new OrderException(ResultEnum.CART_EMPTY);
        }

        // 创建订单
        OrderDTO result = orderService.create(orderDTO);

        // 返回的结果
        HashMap<String, String> map = new HashMap<>();
        map.put("orderId", result.getOrderId());

        return ResultVOUtil.success(map);
    }
}
