package com.zxin.order.utils;

import java.util.Random;

public class KeyUtil {

    /**
     * 生成唯一的主键
     * 格式: 时间戳 + 随机数
     */
    public static synchronized String generateUniqueKey(){
        // 这里只是模拟唯一，不能降低到 100%的唯一
        Random rnd = new Random();
        Integer num = rnd.nextInt(900000) + 100000;
        return System.currentTimeMillis() + String.valueOf(num); // 生成一个随机的串
    }

}
