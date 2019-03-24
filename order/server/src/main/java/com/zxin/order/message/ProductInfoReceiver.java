package com.zxin.order.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.zxin.order.utils.JsonUtil;
import com.zxin.product.common.ProductInfoOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class ProductInfoReceiver {

	// 从MQ中拿到的是一个对象，放到redis中的要存那么多，就存库存的
	private static final String PRODUCT_STOCK_TEMPLATE = "product_stock_%s";

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	//接收MQ消息
	@RabbitListener(queuesToDeclare = @Queue("productInfo")) // productInfo是创建的队列
	public void process(String message) {

		//message => ProductInfoOutput , 将接收到的消息转换成ProductInfoOutput，然后存储到redis中
		List<ProductInfoOutput> productInfoOutputList = (
				List<ProductInfoOutput>) JsonUtil.fromJson(message,
				new TypeReference<List<ProductInfoOutput>>() {});

		log.info("从队列【{}】接收到消息：{}", "productInfo", productInfoOutputList);

		//存储到redis中
		for (ProductInfoOutput productInfoOutput : productInfoOutputList) {
			stringRedisTemplate.opsForValue().set(
					String.format(PRODUCT_STOCK_TEMPLATE, productInfoOutput.getProductId()),
					String.valueOf(productInfoOutput.getProductStock()));
		}
	}
}
