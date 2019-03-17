package com.zxin.product.utils;

import com.zxin.product.viewobject.ResultVO;

/**
 * 返回 ViesObject的工具类
 */
public class ResultVOUtil {

    // 将返回做成了工具类
    public static ResultVO success(Object object){
        ResultVO resultVO = new ResultVO();
        resultVO.setData(object);
        resultVO.setCode(0);
        resultVO.setMsg("成功");
        return resultVO;
    }
}
